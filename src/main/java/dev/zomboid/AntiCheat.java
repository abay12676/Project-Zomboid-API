package dev.zomboid;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringEscapeUtils;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugLog;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.packets.DeadPlayerPacket;
import zombie.network.packets.hit.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static dev.zomboid.ZomboidApi.DISPLAY_NAME;

public class AntiCheat {

    private final Map<String, CustomNetworkData> customNetworkDataMap = new HashMap<>();

    // reflection data to access private zomboid fields..

    private Field playerHitPlayerPacketTarget;
    private Field playerHitPlayerPacketHit;
    private Field playerPlayer;

    private Field playerHitSquarePacketSquare;
    private Field squareX;
    private Field squareY;
    private Field squareZ;

    @Getter
    @Setter
    private AntiCheatCfg cfg = new AntiCheatCfg();

    public AntiCheat() {
        try {
            playerHitPlayerPacketTarget = PlayerHitPlayerPacket.class.getDeclaredField("target");
            playerHitPlayerPacketTarget.setAccessible(true);

            playerHitPlayerPacketHit = PlayerHitPlayerPacket.class.getDeclaredField("hit");
            playerHitPlayerPacketHit.setAccessible(true);

            playerPlayer = Player.class.getDeclaredField("player");
            playerPlayer.setAccessible(true);

            playerHitSquarePacketSquare = PlayerHitSquarePacket.class.getDeclaredField("square");
            playerHitSquarePacketSquare.setAccessible(true);

            squareX = Square.class.getDeclaredField("positionX");
            squareX.setAccessible(true);

            squareY = Square.class.getDeclaredField("positionY");
            squareY.setAccessible(true);

            squareZ = Square.class.getDeclaredField("positionZ");
            squareZ.setAccessible(true);
        } catch (NoSuchFieldException e) {
            DebugLog.log("Failed to initialized hidden field access");
        }
    }

    /**
     * Performs a distance check.
     */
    private boolean distanceCheck(IsoPlayer a, IsoPlayer b, float distance) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2)) < distance;
    }

    /**
     * Performs a distance check.
     */
    private boolean distanceCheck(UdpConnection a, IsoPlayer b, float distance) {
        for (IsoPlayer a2 : a.players) {
            if (a2 != null && distanceCheck(a2, b, distance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Performs a distance check.
     */
    private boolean distanceCheck(IsoPlayer a, float x, float y, float z, float distance) {

        DebugLog.log(a.x + " " + a.y + " " + a.z + " + " + x + " " + y + " " + z);
        return Math.sqrt(Math.pow(a.x - x, 2) + Math.pow(a.y - y, 2) + Math.pow(a.z - z, 2)) < distance;
    }

    /**
     * Performs a distance check.
     */
    private boolean distanceCheck(UdpConnection a, float x, float y, float z, float distance) {
        for (IsoPlayer a2 : a.players) {
            if (a2 != null && distanceCheck(a2, x, y, z, distance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates network data for a player is required, and retrieves it.
     */
    private CustomNetworkData getCustomNetworkData(UdpConnection con, String name) {
        CustomNetworkData data = customNetworkDataMap.computeIfAbsent(name, (n) -> new CustomNetworkData());
        data.connection = con;
        return data;
    }

    /**
     * Retrieves custom network data for a connection.
     */
    private CustomNetworkData getCustomNetworkData(UdpConnection con) {
        for (CustomNetworkData data : customNetworkDataMap.values()) {
            if (data.connection == con) {
                return data;
            }
        }
        return null;
    }

    /**
     * Retrieves custom network data for a connection.
     */
    private CustomNetworkData getCustomNetworkDataAny(UdpConnection con) {
        for (IsoPlayer p : con.players) {
            if (p != null) {
                return getCustomNetworkData(con, p.getUsername());
            }
        }
        return null;
    }

    /**
     * Retrieves a player from a connection.
     */
    private IsoPlayer getPlayerFromConnection(UdpConnection con, int index) {
        if (index < 0) {
            return null;
        }

        if (index >= 4) {
            return null;
        }

        return con.players[index];
    }

    /**
     * Retrieves any player from a connection.
     */
    private IsoPlayer anyPlayerFromConnection(UdpConnection con) {
        for (IsoPlayer p : con.players) {
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * Determines if a player belongs to the provided connection.
     */
    private boolean playerBelongsToConnection(UdpConnection con, IsoPlayer p) {
        for (IsoPlayer p2 : con.players) {
            if (p2 == p) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a connection is allowed to cheat.
     */
    private boolean canCheat(UdpConnection con) {
        return con.accessLevel.equalsIgnoreCase("admin") || con.accessLevel.equalsIgnoreCase("moderator");
    }

    private void reportToDiscord(String msg) {
        DiscordWebhook hook = new DiscordWebhook(cfg.getDiscordApi());
        hook.setUsername(DISPLAY_NAME);

        hook.setContent(StringEscapeUtils.escapeJava(msg));
        hook.setTts(false);

        try {
            hook.execute();
        } catch (IOException e) {
            DebugLog.log("Failed to execute discord hook");
        }
    }

    /**
     * Handles a malformed packet.
     */
    private void handleMalformedPacket(UdpConnection con, ZomboidNetData packet, String reason) {
        if (canCheat(con)) {
            return;
        }

        String name = "No associated player";
        IsoPlayer p = anyPlayerFromConnection(con);
        if (p != null) {
            name = p.getUsername() + " (" + p.getSurname() + " " + p.getForname() + ") Steam: " + p.getSteamID();
        }

        reportToDiscord("## Malformed packet for player __" + name + "__  \nReason: __" + reason + "__");
        DebugLog.log("Handling malformed packet on " + con);
    }

    /**
     * Handles a violation
     */
    private void handleViolation(UdpConnection con, ZomboidNetData packet, String reason) {
        if (canCheat(con)) {
            return;
        }

        DebugLog.log("Handling violation on " + con + " for '" + reason + "'");

        String name = "No associated player";
        String steam = "No associated steam";
        IsoPlayer p = anyPlayerFromConnection(con);
        if (p != null) {
            name = p.getUsername() + " (" + p.getSurname() + " " + p.getForname() + ")";
            steam = Long.toString(p.getSteamID());
        }

        reportToDiscord("## Violation generated for player __" + name + "__  \nSteam: __" + steam + "__  \nReason: __" + reason + "__");
        con.forceDisconnect();
    }

    /**
     * Validates that a synchronized perk value is valid.
     */
    private boolean validateSyncPerk(CustomNetworkData data, Perk perk, int v) {
        if (data.lastKnownPerks.containsKey(perk)) {
            int diff = Math.abs(data.lastKnownPerks.get(perk) - v);
            return diff <= cfg.getEnforceSyncThreshold();
        }

        return true;
    }

    /**
     * Enforces violations for perk synchronization.
     */
    private void enforceSyncPerks(UdpConnection con, ZomboidNetData packet) {
        ByteBuffer b = packet.buffer;

        byte index = b.get();
        int sneak = b.getInt();
        int str = b.getInt();
        int fit = b.getInt();

        IsoPlayer player = getPlayerFromConnection(con, index);
        if (player == null) {
            // this is normal behavior in the land of bad zomboid code..
            // handleMalformedPacket(con, packet, "Player is null (index= " + index + ")");
            return;
        }

        CustomNetworkData data = getCustomNetworkData(con, player.getUsername());
        if (!data.validatePlayer(player)) {
            handleViolation(con, packet, "[enforceSyncPerks] Failed to validate player");
            return;
        }

        if (!validateSyncPerk(data, Perk.SNEAK, sneak)) {
            handleViolation(con, packet, "[enforceSyncPerks] Failed to validate sneak");
            return;
        }

        if (!validateSyncPerk(data, Perk.STR, str)) {
            handleViolation(con, packet, "[enforceSyncPerks] Failed to validate str");
            return;
        }

        if (!validateSyncPerk(data, Perk.FIT, fit)) {
            handleViolation(con, packet, "[enforceSyncPerks] Failed to validate fit");
            return;
        }

        data.lastKnownPerks.put(Perk.SNEAK, sneak);
        data.lastKnownPerks.put(Perk.STR, str);
        data.lastKnownPerks.put(Perk.FIT, fit);
    }

    /**
     * Enforces violations for teleporting.
     */
    private void enforceTeleport(UdpConnection con, ZomboidNetData packet) {
        handleViolation(con, packet, "[enforceTeleport] Not allowed to teleport");
    }

    /**
     * Enforces violations for teleporting.
     */
    private void enforceExtraInfo(UdpConnection con, ZomboidNetData packet) {
        handleViolation(con, packet, "[enforceExtraInfo] Not allowed to send extra info");
    }

    /**
     * Enforces violations for player deaths.
     */
    private void enforceSendPlayerDeath(UdpConnection con, ZomboidNetData packet) {
        DeadPlayerPacket dp = new DeadPlayerPacket();
        dp.parse(packet.buffer);

        IsoPlayer target = dp.getPlayer();
        if (!playerBelongsToConnection(con, target)) {
            handleViolation(con, packet, "[enforceSendPlayerDeath] Sending player death to other player");
        }
    }

    /**
     * Enforces violations for additional pain.
     */
    private void enforceAdditionalPain(UdpConnection con, ZomboidNetData packet) {
        handleViolation(con, packet, "[enforceAdditionalPain] Sending additional pain packet");
    }

    /**
     * Enforces violations for players hitting other players.
     */
    private void enforcePlayerHitSquarePacket(UdpConnection con, ZomboidNetData packet, HitCharacterPacket hcp) {
        PlayerHitSquarePacket hp = (PlayerHitSquarePacket) hcp;
        try {
            Square sq = (Square) playerHitSquarePacketSquare.get(hp);
            float x = (float) squareX.get(sq);
            float y = (float) squareY.get(sq);
            float z = (float) squareZ.get(sq);
            if (!distanceCheck(con, x, y, z, 100.f)) {
                handleViolation(con, packet, "[enforcePlayerHitSquarePacket] Player too far from hit");
                return;
            }
        } catch (IllegalAccessException e) {
            DebugLog.log("Failed to read square info");
        }
    }

    /**
     * Enforces an incoming packet.
     */
    public void enforce(UdpConnection con, ZomboidNetData packet) {
        short id = packet.type;

        if (cfg.isRateLimited(id)) {
            CustomNetworkData data = getCustomNetworkDataAny(con);
            if (data != null) {
                RateLimiter limiter = data.createRateLimiter(id, cfg.getRateLimit(id));
                if (!limiter.check()) {
                    handleViolation(con, packet, "Rate limiting");
                    return;
                }
            }
        }

        if (cfg.isEnforceSyncPerks()) {
            if (id == PacketTypes.PacketType.SyncPerks.getId()) {
                enforceSyncPerks(con, packet);
            }
        }

        if (cfg.isEnforceTeleport()) {
            if (id == PacketTypes.PacketType.Teleport.getId()) {
                enforceTeleport(con, packet);
            }
        }

        if (cfg.isEnforceExtraInfo()) {
            if (id == PacketTypes.PacketType.ExtraInfo.getId()) {
                enforceExtraInfo(con, packet);
            }
        }

        if (cfg.isEnforcePlayerDeaths()) {
            if (id == PacketTypes.PacketType.PlayerDeath.getId()) {
                enforceSendPlayerDeath(con, packet);
            }
        }

        if (cfg.isEnforceAdditionalPain()) {
            if (id == PacketTypes.PacketType.AdditionalPain.getId()) {
                enforceAdditionalPain(con, packet);
            }
        }

        if (id == PacketTypes.PacketType.HitCharacter.getId()) {
            HitCharacterPacket hcp = HitCharacterPacket.process(packet.buffer);
            hcp.parse(packet.buffer);
            if (hcp instanceof PlayerHitSquarePacket) {
                enforcePlayerHitSquarePacket(con, packet, hcp);
            }
        }
    }

    private enum Perk {
        SNEAK,
        STR,
        FIT,
    }

    private class CustomNetworkData {
        private final Map<Perk, Integer> lastKnownPerks = new HashMap<>();
        private final Map<Short, RateLimiter> rateLimiters = new HashMap<>();
        private UdpConnection connection;
        private IsoPlayer player;

        public RateLimiter createRateLimiter(short type, long delay) {
            return rateLimiters.computeIfAbsent(type, (t) -> new RateLimiter(delay));
        }

        public boolean validatePlayer(IsoPlayer p) {
            if (player == null) {
                player = p;
            }

            return player == p;
        }
    }
}

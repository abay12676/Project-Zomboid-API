package dev.zomboid.admin;

import lombok.experimental.UtilityClass;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.GameClient;
import zombie.network.PacketTypes;
import zombie.network.packets.PlayerPacket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Cheats {

    public static IsoPlayer primaryPlayer() {
        return GameClient.connection.players[0];
    }

    /**
     * Performs a safe teleport on a player by only moving them a certain amount at a time. This can cause
     * de-synchronization for remote players, leading to them being moved to invalid locations.
     */
    public static void safeTeleport(IsoPlayer player, float x, float y, float z) {
        float deltaX = x - player.x;
        float deltaY = y - player.y;
        float deltaZ = z - player.z;
        float remX = Math.abs(deltaX);
        float remY = Math.abs(deltaY);
        float remZ = Math.abs(deltaZ);
        while (remX > 0.f || remY > 0.f || remZ > 0.f) {
            float moveX = Math.min(remX, 3.9f);
            float moveY = Math.min(remY, 3.9f);
            float moveZ = Math.min(remZ, 3.9f);

            remX -= moveX;
            remY -= moveY;
            remZ -= moveZ;

            if (deltaX < 0.f) {
                moveX = -moveX;
            }

            if (deltaY < 0.f) {
                moveY = -moveY;
            }

            if (deltaZ < 0.f) {
                moveZ = -moveZ;
            }

            player.setX(player.x + moveX);
            player.setY(player.y + moveY);
            player.setZ(player.z + moveZ);
            player.setLx(player.getX());
            player.setLy(player.getY());
            player.setLz(player.getZ());

            GameClient.instance.sendPlayer(player);

            if (PlayerPacket.l_send.playerPacket.set(player)) {
                ByteBufferWriter writer = GameClient.connection.startPacket();

                PacketTypes.PacketType.PlayerUpdateReliable.doPacket(writer);
                PlayerPacket.l_send.playerPacket.write(writer);
                PacketTypes.PacketType.PlayerUpdateReliable.send(GameClient.connection);
            }
        }
    }

    /**
     * Kills a player.
     *
     * @param player The player to kill.
     * @param type The method to use for killing the player.
     */
    public static void kill(IsoPlayer player, KillType type) {
        IsoPlayer local = primaryPlayer();
        if (local == null) {
            return;
        }

        switch (type) {
            case LOCAL_DEATH:
                GameClient.instance.sendPlayerDeath(player);
                break;
            case NORMAL_HIT:
                GameClient.sendHitCharacter(local, player, null,
                        10000.f /* dmg */,
                        false /* ignore */,
                        10000.f /* range */,
                        false /* unknown player flags */,
                        false /* unknown zombie flags */,
                        true /* hit head */
                        );
                break;
        }
    }

    /**
     * Kills all players.
     *
     * @param type The method to use for killing the players.
     */
    public static void killAllPlayers(KillType type) {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            kill(p, type);
        }
    }

    /**
     * Kills all players with bruteforce.
     *
     * @param type The method to use for killing the players.
     */
    public static void killAllPlayersBrute(KillType type) {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            kill(p, KillType.LOCAL_DEATH);
        }
    }

    public static void damageBody(IsoPlayer player, BodyPartType part, float damage) {
        GameClient.instance.sendAdditionalPain(player.OnlineID, part.index(), damage);
    }

    public static void damageBodies(BodyPartType part, float damage) {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            damageBody(p, part, damage);
        }
    }

    public static void killAllZombies() {
        IsoCell cell = IsoWorld.instance.CurrentCell;
        if (cell != null) {
            for (IsoZombie z : cell.getZombieList()) {
                GameClient.sendKillZombie(z);
            }
        }
    }

    public static void killAllZombiesBruteforce() {
        IsoZombie z = new IsoZombie(null);
        for (int i = 0; i < 5000; i++) {
            z.OnlineID = (short)i;
            GameClient.sendKillZombie(z);
        }
    }

    public static void rainbowObjects(IsoPlayer player) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        IsoCell cell = player.getCell();
        if (cell != null) {

            for (IsoObject object : cell.getObjectList()) {
                if (object.square != null) {
                    object.setCustomColor(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat());
                    GameClient.instance.sendCustomColor(object);
                }
            }
        }
    }

    public static void addCorpseToMap(IsoGridSquare square) throws IOException {
        ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
        byteBufferWriter.putByte((byte)-122);
        for (int i = 0; i < 4; i++) {
            byteBufferWriter.putByte((byte)ThreadLocalRandom.current().nextInt());
        }
        GameClient.connection.endPacketImmediate();
    }

    public static void startFire(IsoPlayer player, boolean smoke) {
        IsoGridSquare square = player.getSquare();
        if (square != null) {
            ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
            PacketTypes.PacketType.StartFire.doPacket(byteBufferWriter);
            byteBufferWriter.putInt(square.getX());
            byteBufferWriter.putInt(square.getY());
            byteBufferWriter.putInt(square.getZ());
            byteBufferWriter.putInt(10000); // energy
            byteBufferWriter.putByte((byte) 1); // idk not used
            byteBufferWriter.putInt(10000); // life
            byteBufferWriter.putByte((byte) (smoke ? 1 : 0)); // smoke
            PacketTypes.PacketType.StartFire.send(GameClient.connection);
        }
    }

    public static void bindConnectionSlot(UdpConnection con, int slot) {
        ByteBufferWriter byteBufferWriter = con.startPacket();
        byteBufferWriter.putByte((byte) 19);
        byteBufferWriter.putByte((byte) slot);
        con.endPacket(1, 3, (byte)0);
    }

    public static void bindConnectionSlots(UdpConnection con) {
        for (int i = 0; i < 0x100; i++) {
            bindConnectionSlot(con, i);
        }
    }

    public static void disconnectSlot(UdpConnection con, int slot) {
        ByteBufferWriter byteBufferWriter = con.startPacket();
        byteBufferWriter.putByte((byte) 21);
        byteBufferWriter.putByte((byte) slot);

        con.endPacketImmediate();
    }

    public static void disconnectSlots(UdpConnection con) {
        int localId = -1;
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            if (p.isLocalPlayer()) {
                localId = p.getOnlineID();
            }
        }
        for (int i = 0; i < 0x100; i++) {
            if (i != GameClient.connection.index) {
                disconnectSlot(con, i);
            }
        }
    }
}

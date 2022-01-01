package dev.zomboid.cheat;

import lombok.experimental.UtilityClass;
import zombie.characterTextures.BloodBodyPartType;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.core.ImmutableColor;
import zombie.core.network.ByteBufferWriter;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.objects.IsoFire;
import zombie.network.GameClient;
import zombie.network.PacketTypes;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Cheat {

    public static List<IsoPlayer> listPlayers() {
        return GameClient.instance.getPlayers();
    }

    public static void kill(IsoPlayer player) {
        GameClient.instance.sendPlayerDeath(player);
    }

    public static void killAllPlayers() {
        for (IsoPlayer p : listPlayers()) {
            kill(p);
        }
    }

    public static void damageBody(IsoPlayer player, BodyPartType part, float damage) {
        GameClient.instance.sendAdditionalPain(player.OnlineID, part.index(), damage);

    }

    public static void killAllZombies() {
        IsoCell cell = IsoWorld.instance.CurrentCell;
        if (cell != null) {
            for (IsoZombie z : cell.getZombieList()) {
                GameClient.sendKillZombie(z);
            }
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

    public static void blackify(IsoPlayer player) {
        player.getHumanVisual().setHairColor(new ImmutableColor(0, 0, 0, 255));
        player.getHumanVisual().setBeardColor(new ImmutableColor(0, 0, 0, 255));
        player.getHumanVisual().setSkinColor(new ImmutableColor(0, 0, 0, 255));
        player.getHumanVisual().setBodyHairIndex(0);
        player.getHumanVisual().setHole(BloodBodyPartType.Groin);
        player.getHumanVisual().setDirt(BloodBodyPartType.Groin, 100.f);

        GameClient.instance.sendVisual(player);
    }

    public static void fuck(IsoPlayer player) {
        player.getHumanVisual().setBeardModel("");
        player.getHumanVisual().setHairModel("");
    }

    public static void startFire(IsoPlayer player) {
        IsoGridSquare square = player.getSquare();
        if (square != null) {
            IsoFire f;

            ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
            PacketTypes.PacketType.StartFire.doPacket(byteBufferWriter);
            byteBufferWriter.putInt(square.getX());
            byteBufferWriter.putInt(square.getY());
            byteBufferWriter.putInt(square.getZ());
            byteBufferWriter.putInt(10000); // energy
            byteBufferWriter.putByte((byte) 1); // idk not used
            byteBufferWriter.putInt(10000); // life
            byteBufferWriter.putByte((byte) 0); // idk not used
            PacketTypes.PacketType.StartFire.send(GameClient.connection);
            System.out.println("Fire!!!");
        }

    }
}

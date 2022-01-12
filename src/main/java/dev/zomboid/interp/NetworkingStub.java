package dev.zomboid.interp;

import dev.zomboid.admin.Cheats;
import lombok.experimental.UtilityClass;
import se.krka.kahlua.vm.KahluaTable;
import zombie.GameWindow;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.RakNetPeerInterface;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.debug.DebugLog;
import zombie.iso.SpawnPoints;
import zombie.network.GameClient;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static dev.zomboid.ZomboidApi.core;
import static zombie.network.PacketTypes.PacketType.GetTableResult;
import static zombie.network.PacketTypes.PacketType.SyncPerks;

/**
 * Provides interpolation with the game's networking code.
 */
@UtilityClass
public class NetworkingStub {

    /**
     * Called before the game runs GameServer#addIncoming.
     */
    public static void addIncomingServer(short opcode, ByteBuffer data, UdpConnection connection) {
        ZomboidNetData packet = ZomboidNetDataPool.instance.getLong(data.limit());
        data.mark();
        packet.read(opcode, data, connection);
        data.reset();
        core.antiCheat.enforce(connection, packet);
    }

    /**
     * Called before the game runs GameClient#addIncoming.
     */
    public static void addIncomingClient(short opcode, ByteBuffer data) {
        ZomboidNetData packet = ZomboidNetDataPool.instance.getLong(data.limit());
        data.mark();
        packet.read(opcode, data, GameClient.connection);
        data.reset();

        // the game has no handling for this packet making it impossible to
        // get the results from a database query normally...
        // what fun is that?
        if (packet.type == GetTableResult.getId()) {
            int id = packet.buffer.getInt();
            String table = GameWindow.ReadString(packet.buffer);
            DebugLog.log("Received database query '" + table + "'");

            int rows = packet.buffer.getInt();
            for (int i = 0; i < rows; i++) {
                int columns = packet.buffer.getInt();
                for (int j = 0; j < columns; j++) {
                    String key = GameWindow.ReadString(packet.buffer);
                    String val = GameWindow.ReadString(packet.buffer);
                    DebugLog.log(" -> " + key + ":" + val);
                }
            }
        }
    }

    /**
     * Called before the game runs UdpEngine#decode.
     */
    public static void decode(UdpEngine engine, ByteBuffer b) {
        b.mark();
        int type = b.get() & 0xff;
        System.out.println("... " + type);
        if (type == RakNetPeerInterface.ID_CONNECTION_REQUEST_ACCEPTED) {
            // seutp connection manually...
            int index = b.get() & 0xff;
            long guid = engine.getPeer().getGuidOfPacket();
            GameClient.connection = new UdpConnection(engine, guid, index);

            ThreadLocalRandom r = ThreadLocalRandom.current();
            List<Integer> rnd = new LinkedList<>();
            for (int i = 0; i < 255; i++) {
                rnd.add(i);
            }

            for (int i = 0; i< 1000; i++) {
                int xi = r.nextInt(rnd.size());
                int x = rnd.get(xi);

                int yi = r.nextInt(rnd.size());
                int y = rnd.get(yi);

                rnd.set(xi, y);
                rnd.set(yi, x);
            }

            for (int i : rnd) {
                //Cheats.disconnectSlot(GameClient.connection, i);
            }
            //Cheats.killAllZombiesBruteforce();
        }
        b.reset();
    }
}

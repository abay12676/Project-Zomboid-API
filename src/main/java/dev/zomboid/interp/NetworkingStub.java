package dev.zomboid.interp;

import lombok.experimental.UtilityClass;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;

import static dev.zomboid.ZomboidApi.core;

/**
 * Provides interpolation with the game's networking code.
 */
@UtilityClass
public class NetworkingStub {

    /**
     * Called before the game runs GameServer#addIncoming.
     */
    public static void addIncoming(short opcode, ByteBuffer data, UdpConnection connection) {
        ZomboidNetData packet = ZomboidNetDataPool.instance.getLong(data.limit());
        data.mark();
        packet.read(opcode, data, connection);
        data.reset();
        core.antiCheat.enforce(connection, packet);
    }
}

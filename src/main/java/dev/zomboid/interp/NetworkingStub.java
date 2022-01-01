package dev.zomboid.interp;

import dev.zomboid.AntiCheat;
import dev.zomboid.RateLimiter;
import lombok.experimental.UtilityClass;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static dev.zomboid.ZomboidApi.*;

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

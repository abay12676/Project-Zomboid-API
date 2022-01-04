package dev.zomboid.cheat;

import dev.zomboid.ZomboidApi;
import dev.zomboid.extend.AbstractEventHandler;
import zombie.characters.IsoPlayer;
import zombie.network.GameClient;
import zombie.ui.NewWindow;
import zombie.ui.UIElement;
import zombie.ui.UIFont;
import zombie.ui.UITextBox2;

import java.util.*;

public class CheatWindow extends NewWindow {

    private final UITextBox2 nameBox;
    private final List<CheatPlayer> orderedElements = new LinkedList<>();
    private final Map<IsoPlayer, CheatPlayer> playerMap = new HashMap<>();
    private IsoPlayer target = null;
    private long nextPacketTime = 0;

    public CheatWindow() {
        super(15, 15, 315, 1200, false);

        AddChild(nameBox = new UITextBox2(UIFont.Small, 0, 0, 300, 20, ZomboidApi.DISPLAY_NAME, true));
        nameBox.setEditable(false);

        AddChild(new CheatButton(new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheat.killAllPlayers();
            }
        }, 5, 25, "Kill All Players", "kill_all_players_button"));

        AddChild(new CheatButton(new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheat.killAllZombies();
            }
        }, 90, 25, "Kill All Zombies", "kill_all_zombies_button"));

        AddChild(new CheatButton(new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                IsoPlayer local = null;
                for (IsoPlayer p2 : GameClient.instance.getPlayers()) {
                    if (p2.isLocalPlayer()) {
                        local = p2;
                        break;
                    }
                }

                if (local != null) {
                    for (IsoPlayer p2 : GameClient.instance.getPlayers()) {
                        GameClient.sendTeleport(p2, local.x, local.y, local.z);
                    }
                }
            }
        }, 175, 25, "Teleport All", "teleport_all_button"));
    }

    private void rebasePlayers() {
        int y = 50;
        for (CheatPlayer cp : orderedElements) {
            for (UIElement e : cp.elements) {
                e.y = y;
            }

            y += 25;
        }
    }

    private void createNewPlayers() {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            if (!playerMap.containsKey(p)) {
                CheatPlayer cp = new CheatPlayer();

                UITextBox2 nameLabel = new UITextBox2(UIFont.Small, 5, 0, 120, 20, p.getDisplayName(), true);
                cp.elements.add(nameLabel);

                CheatButton killButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        Cheat.kill(p);
                    }
                }, 130, 0, "Kill", "kill_player_" + p.getDisplayName());
                cp.elements.add(killButton);

                CheatButton rainbowObjsButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        Cheat.rainbowObjects(p);
                    }
                }, 195, 0, "Disco", "disco_player_" + p.getDisplayName());
                cp.elements.add(rainbowObjsButton);

                CheatButton blackifyButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        Cheat.blackify(p);
                        for (int j = 0; j < 100; j++) {
                            Cheat.startFire(p);
                        }

                        for (IsoPlayer p2 : GameClient.instance.getPlayers()) {
                            if (p2.isLocalPlayer()) {
                                System.out.println("Teleport niggg");
                                GameClient.sendTeleport(p2, p.x, p.y, p.z);
                            }
                        }

                        target = p;
                    }
                }, 260, 0, "Burn", "burn_player_" + p.getDisplayName());
                cp.elements.add(blackifyButton);

                playerMap.put(p, cp);
                orderedElements.add(cp);

                AddChild(nameLabel);
                AddChild(killButton);
                AddChild(rainbowObjsButton);
                AddChild(blackifyButton);
            }
        }
    }

    private void removeOldPlayers() {
        Iterator<IsoPlayer> it = playerMap.keySet().iterator();
        while (it.hasNext()) {
            IsoPlayer p = it.next();
            CheatPlayer cp = playerMap.get(p);
            if (!GameClient.instance.getPlayers().contains(p)) {
                for (UIElement e : cp.elements) {
                    RemoveChild(e);
                }
                orderedElements.remove(cp);
                it.remove();
            }
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void update() {
        visible = true;
        setAlwaysOnTop(true);
        removeOldPlayers();
        createNewPlayers();
        rebasePlayers();

        long t = System.currentTimeMillis();
        if (t > nextPacketTime) {
            if (target != null) {
                Cheat.startFire(target);
            }
            nextPacketTime = (t + 50);
        }
        super.update();
    }

    private class CheatPlayer {
        private final List<UIElement> elements = new LinkedList<>();
    }
}

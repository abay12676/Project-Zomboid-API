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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class CheatWindow extends NewWindow {

    private final List<CheatPlayer> orderedElements = new LinkedList<>();
    private final Map<IsoPlayer, CheatPlayer> playerMap = new HashMap<>();
    private long target;
    private long nextPacketTime = 0;

    private boolean targetFire = false;
    private boolean targetSmoke = false;
    private boolean targetKill = false;

    private void pushButtonHorizontal(AtomicInteger x, int y, String text, String name, AbstractEventHandler handler) {
        CheatButton button = new CheatButton(handler, x.get(), y, text, name);
        AddChild(button);
        x.addAndGet(button.getWidth().intValue());
        x.addAndGet(5);
    }

    private void pushCheckBoxHorizontal(AtomicInteger x, int y, String text, String name, AbstractEventHandler handler) {
        CheatCheckBox box = new CheatCheckBox(handler, x.get(), y, text, name);
        AddChild(box);
        x.addAndGet(box.getWidth().intValue());
        x.addAndGet(5);
    }

    public CheatWindow() {
        super(15, 15, 315, 1200, false);

        UITextBox2 nameBox = new UITextBox2(UIFont.Small, 0, 0, 300, 20, ZomboidApi.DISPLAY_NAME, true);
        AddChild(nameBox);
        nameBox.setEditable(false);

        AtomicInteger x = new AtomicInteger(5);
        pushButtonHorizontal(x, 25, "Kill All Players", "kill_all_players_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheat.killAllPlayers();
            }
        });

        pushButtonHorizontal(x, 25, "Kill All Zombies", "kill_all_zombies_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheat.killAllZombies();
            }
        });

        pushButtonHorizontal(x, 25, "Teleport All", "teleport_all_button", new AbstractEventHandler() {
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
        });

        x.set(5);
        pushCheckBoxHorizontal(x, 25, "Target Fire", "target_fire_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetFire = (toggled == 1);
            }
        });

        pushCheckBoxHorizontal(x, 25, "Target Smoke", "target_smoke_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetSmoke = (toggled == 1);
            }
        });

        pushCheckBoxHorizontal(x, 25, "Target Kill", "target_kill_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetKill = (toggled == 1);
            }
        });

        pushCheckBoxHorizontal(x, 25, "God", "god_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                for (IsoPlayer p : GameClient.instance.getPlayers()) {
                    if (p.isLocalPlayer()) {
                        p.setGodMod(toggled == 1);
                        p.setNoClip(toggled == 1);
                        p.setCanHearAll(toggled == 1);
                        p.setInvisible(toggled == 1);
                        p.setGhostMode(toggled == 1);
                        GameClient.sendPlayerExtraInfo(p);
                    }
                }
            }
        });
    }

    private void rebasePlayers() {
        setHeight(120 + (25 * orderedElements.size()));

        int y = 68;
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

                int x = 5;
                UITextBox2 nameLabel = new UITextBox2(UIFont.Small, x, 0, 120, 20, p.getDisplayName(), true);
                cp.elements.add(nameLabel);
                x += nameLabel.getWidth();
                x += 5;

                CheatButton killButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        Cheat.kill(p);
                    }
                }, x, 0, "Kill", "kill_player_" + p.getDisplayName());
                cp.elements.add(killButton);
                x += killButton.getWidth();
                x += 5;

                CheatButton lagButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        float scale = 1.f;
                        float offX = (ThreadLocalRandom.current().nextFloat() * scale) - scale;
                        float offY = (ThreadLocalRandom.current().nextFloat() * scale) - scale;
                        float offZ = (ThreadLocalRandom.current().nextFloat() * scale) - scale;

                        GameClient.sendTeleport(p, p.x + offX, p.y + offY, p.z + offZ);
                    }
                }, x, 0, "Lag", "lag_player_" + p.getDisplayName());
                cp.elements.add(lagButton);
                x += lagButton.getWidth();
                x += 5;

                CheatButton teleportButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        for (IsoPlayer p2 : GameClient.instance.getPlayers()) {
                            if (p2.isLocalPlayer()) {
                                GameClient.sendTeleport(p2, p.x, p.y, p.z);
                            }
                        }
                    }
                }, x, 0, "Tele", "teleport_player_" + p.getDisplayName());
                cp.elements.add(teleportButton);
                x += teleportButton.getWidth();
                x += 5;

                CheatButton targetButton = new CheatButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        target = p.getSteamID();
                    }
                }, x, 0, "Target", "target_player_" + p.getDisplayName());
                cp.elements.add(targetButton);
                x += targetButton.getWidth();
                x += 5;

                AddChild(nameLabel);
                AddChild(killButton);
                AddChild(lagButton);
                AddChild(teleportButton);
                AddChild(targetButton);

                playerMap.put(p, cp);
                orderedElements.add(cp);
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

    private IsoPlayer findTarget() {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            if (p.getSteamID() == target) {
                return p;
            }
        }
        return null;
    }

    /**
     * Applies any selected effects to the target.
     */
    private void applyTargetEffects() {
        IsoPlayer t = findTarget();
        if (t != null) {
            if (targetFire) {
                Cheat.startFire(t, true);
            }

            if (targetSmoke) {
                Cheat.startFire(t, false);
            }

            if (targetKill) {
                Cheat.kill(t);
            }
        }
    }

    /**
     * Runs the cheat timer.
     */
    private void timer() {
        long t = System.currentTimeMillis();
        if (t > nextPacketTime) {
            applyTargetEffects();
            nextPacketTime = (t + ThreadLocalRandom.current().nextInt(900, 1400));
        }
    }

    @Override
    public void update() {
        visible = true;
        removeOldPlayers();
        createNewPlayers();
        rebasePlayers();

        timer();
        super.update();
    }

    private class CheatPlayer {
        private final List<UIElement> elements = new LinkedList<>();
    }
}

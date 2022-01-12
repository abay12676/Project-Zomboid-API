package dev.zomboid.admin;

import dev.zomboid.ZomboidApi;
import dev.zomboid.extend.AbstractEventHandler;
import lombok.RequiredArgsConstructor;
import zombie.SoundManager;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatManager;
import zombie.core.network.ByteBufferWriter;
import zombie.iso.IsoGridSquare;
import zombie.iso.objects.IsoMannequin;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.network.GameClient;
import zombie.network.PacketTypes;
import zombie.network.chat.ChatType;
import zombie.ui.NewWindow;
import zombie.ui.UIElement;
import zombie.ui.UIFont;
import zombie.ui.UITextBox2;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminToolWindow extends NewWindow {

    private final List<CheatPlayer> orderedElements = new LinkedList<>();
    private final Map<IsoPlayer, CheatPlayer> playerMap = new HashMap<>();
    private long target;
    private long nextPacketTime = 0;

    private boolean targetFire = false;
    private boolean targetSmoke = false;
    private boolean targetKill = false;

    private int yShift = 0;

    private void pushButtonHorizontal(AtomicInteger x, int y, String text, String name, AbstractEventHandler handler) {
        AdminToolButton button = new AdminToolButton(handler, x.get(), y, text, name);
        AddChild(button);
        x.addAndGet(button.getWidth().intValue());
        x.addAndGet(5);
    }

    private void pushCheckBoxHorizontal(AtomicInteger x, int y, String text, String name, AbstractEventHandler handler) {
        AdminToolCheckBox box = new AdminToolCheckBox(handler, x.get(), y, text, name);
        AddChild(box);
        x.addAndGet(box.getWidth().intValue());
        x.addAndGet(5);
    }

    public AdminToolWindow() {
        super(15, 15, 415, 1200, false);

        UITextBox2 nameBox = new UITextBox2(UIFont.Small, 0, 0, 300, 20, ZomboidApi.DISPLAY_NAME, true);
        AddChild(nameBox);
        nameBox.setEditable(false);

        AtomicInteger x = new AtomicInteger(5);
        pushButtonHorizontal(x, 25, "Kill All Players", "kill_all_players_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheats.killAllPlayers(KillType.NORMAL_HIT);
            }
        });
        pushButtonHorizontal(x, 25, "Hurt All Players", "hurt_all_players_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheats.damageBodies(BodyPartType.Groin, 100.f);
            }
        });

        pushButtonHorizontal(x, 25, "Kill All Zombies", "kill_all_zombies_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int i, int i1) {
                Cheats.killAllZombies();
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
                        Cheats.safeTeleport(p2, local.x, local.y, local.z);
                    }
                }
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

        x.set(5);
        pushCheckBoxHorizontal(x, 45, "Target Fire", "target_fire_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetFire = (toggled == 1);
            }
        });

        pushCheckBoxHorizontal(x, 45, "Target Smoke", "target_smoke_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetSmoke = (toggled == 1);
            }
        });

        pushCheckBoxHorizontal(x, 45, "Target Kill", "target_kill_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                targetKill = (toggled == 1);
            }
        });

        pushButtonHorizontal(x, 45, "Dump DB", "dump_db", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                GameClient.instance.getTableResult("whitelist", 10000);
            }
        });

        pushButtonHorizontal(x, 45, "Up", "up_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                if (yShift > 0) {
                    yShift -= 1;
                }
            }
        });

        pushButtonHorizontal(x, 45, "Down", "down_button", new AbstractEventHandler() {
            @Override
            public void Selected(String s, int toggled, int i1) {
                if (yShift < (orderedElements.size() - 1)) {
                    yShift += 1;
                }
            }
        });
    }

    private void updateName(IsoPlayer p, UITextBox2 box) {
        StringBuilder name = new StringBuilder();
        name.append(p.getDisplayName());
        if (p.accessLevel.equalsIgnoreCase("admin")) {
            name.append("*");
        }

        if (p.isInvisible()) {
            name.append("!");
        }

        box.SetText(name.toString());
    }

    private void rebasePlayers() {
        int count = Math.min(10, orderedElements.size() - yShift);
        setHeight(Math.max(72 + (count * 25), 80));

        for (CheatPlayer cp : orderedElements) {
            for (UIElement e : cp.elements) {
                e.visible = false;
            }
        }

        if (count < 0) {
            return;
        }

        int y = 68;
        for (int i = yShift; i < yShift + count; i++) {
            CheatPlayer cp = orderedElements.get(i);
            updateName(cp.player, (UITextBox2) cp.elements.get(0));

            for (UIElement e : cp.elements) {
                e.y = y;
                e.visible = true;
            }

            y += 25;
        }
    }

    private void createNewPlayers() {
        for (IsoPlayer p : GameClient.instance.getPlayers()) {
            if (!playerMap.containsKey(p)) {
                CheatPlayer cp = new CheatPlayer(p);

                int x = 5;
                UITextBox2 nameLabel = new UITextBox2(UIFont.Small, x, 0, 120, 20, "", true);
                cp.elements.add(nameLabel);
                x += nameLabel.getWidth();
                x += 5;

                AdminToolButton killButton = new AdminToolButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        Cheats.kill(p, KillType.NORMAL_HIT);
                    }
                }, x, 0, "Kill", "kill_player_" + p.getDisplayName());
                cp.elements.add(killButton);
                x += killButton.getWidth();
                x += 5;

                AdminToolButton lagButton = new AdminToolButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        float scale = 1.f;
                        float offX = (ThreadLocalRandom.current().nextFloat() * scale) - scale;
                        float offY = (ThreadLocalRandom.current().nextFloat() * scale) - scale;
                        float offZ = (ThreadLocalRandom.current().nextFloat() * scale) - scale;

                        Cheats.safeTeleport(p, p.x + offX, p.y + offY, p.z + offZ);
                    }
                }, x, 0, "Lag", "lag_player_" + p.getDisplayName());
                cp.elements.add(lagButton);
                x += lagButton.getWidth();
                x += 5;

                AdminToolButton teleportButton = new AdminToolButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        for (IsoPlayer p2 : GameClient.instance.getPlayers()) {
                            if (p2.isLocalPlayer()) {
                                Cheats.safeTeleport(p2, p.x, p.y, p.z);
                            }
                        }
                    }
                }, x, 0, "Tele", "teleport_player_" + p.getDisplayName());
                cp.elements.add(teleportButton);
                x += teleportButton.getWidth();
                x += 5;

                AdminToolButton earRapeButton = new AdminToolButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        IsoGridSquare sq = p.square;
                        if (sq != null) {
                            SoundManager.instance.PlayWorldSound("BurnedObjectExploded", sq, 100.f, 100.f, 100.f, true);

                            IsoMannequin manne = new IsoMannequin(p.getCell());
                            manne.square = sq;
                            manne.doNotSync = false;
                            manne.setOutlineOnMouseover(true);

                            manne.setSprite(IsoSpriteManager.instance.getSprite("furniture_tables_high_01_37"));

                            sq.getObjects().add(manne);

                            ByteBufferWriter byteBufferWriter = GameClient.connection.startPacket();
                            PacketTypes.PacketType.AddItemToMap.doPacket(byteBufferWriter);
                            byteBufferWriter.putByte((byte)24);
                            manne.writeToRemoteBuffer(byteBufferWriter);
                            PacketTypes.PacketType.AddItemToMap.send(GameClient.connection);

                            sq.getObjects().remove(manne);
                        }
                    }
                }, x, 0, "Ear Rape", "ear_rape_player_" + p.getDisplayName());
                cp.elements.add(earRapeButton);
                x += earRapeButton.getWidth();
                x += 5;

                AdminToolButton targetButton = new AdminToolButton(new AbstractEventHandler() {
                    @Override
                    public void Selected(String s, int i, int i1) {
                        target = p.getSteamID();

                        try {
                            Field f = ChatManager.class.getDeclaredField("player");
                            f.setAccessible(true);
                            f.set(ChatManager.getInstance(), p);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, x, 0, "Target", "target_player_" + p.getDisplayName());
                cp.elements.add(targetButton);
                x += targetButton.getWidth();
                x += 5;

                AddChild(nameLabel);
                AddChild(killButton);
                AddChild(lagButton);
                AddChild(teleportButton);
                AddChild(earRapeButton);
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
                Cheats.startFire(t, true);
            }

            if (targetSmoke) {
                Cheats.startFire(t, false);
            }

            if (targetKill) {
                Cheats.kill(t, KillType.NORMAL_HIT);
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
        setAlwaysOnTop(true);

        removeOldPlayers();
        createNewPlayers();
        rebasePlayers();

        timer();
        super.update();
    }

    @RequiredArgsConstructor
    private class CheatPlayer {
        private final IsoPlayer player;
        private final List<UIElement> elements = new LinkedList<>();
    }
}

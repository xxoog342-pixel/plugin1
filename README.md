# Onyx

Full Java plugin rebuild of your ONUX weapon arsenal — Warden Crossbow,
Frost Blade, Leviathan's Fang, Void Blade, and the new Eyes weapon — built
on top of the package/command layout from the `Onyx.jar` you uploaded
(`com.onux.onyx`, same command names, same `pom` coordinates `com.onux:onyx`).

## Important limitations, read this first

This was built in a sandboxed environment with **no internet access and
no `javac`/decompiler installed**. That means two things:

1. **I could not decompile your `Onyx.jar`.** I read its file/class/package
   names (readable in the zip index) to match this rebuild to your existing
   structure, but the actual method bodies inside your compiled classes were
   opaque to me. So this is a from-scratch reimplementation under the same
   architecture, not a patch on your original bytecode.
2. **I could not compile this project.** Everything here is hand-written
   against the standard Paper 1.21.x API and I'm confident in it, but you
   should do a `mvn clean package` yourself before trusting it on a live
   server, and skim the console for any compiler errors on your end (see
   "If it doesn't compile" below).

## Building

```
cd Onyx
mvn clean package
```

The jar comes out at `target/Onyx-2.0.0.jar` — drop it in your server's
`plugins/` folder. Requires Java 21 and a Paper 1.21.x server.

If `1.21.11-R0.1-SNAPSHOT` isn't resolvable from
`https://repo.papermc.io/repository/maven-public/` yet, open
`pom.xml` and bump `<paper.api.version>` to whatever the newest 1.21.x
build is — nothing in this codebase needs anything newer than the
1.20.5 API baseline (Adventure components, PersistentDataContainer,
the modern `Particle` set, `BossBar`).

### If it doesn't compile

The two likeliest culprits on a real toolchain:
- A `Particle` enum name has since been renamed upstream — every particle
  call goes through `util/FX.java`, so it's a one-file fix.
- An `Enchantment` static field (`SHARPNESS`, `UNBREAKING`, `QUICK_CHARGE`)
  got removed in favor of `Registry.ENCHANTMENT.get(NamespacedKey...)` —
  only `weapons/WeaponFactory.java` touches those.

## What's in the kit

**Warden Crossbow**
- Ability 1 — **Warden Beam**: hold Shift + right-click to charge, release
  to fire. 60s cooldown, 3-heart damage, strong knockback — both numbers
  straight from your spec.
- Ability 2 — **Sculk Meteor**: look at a player, press F. Ring forms,
  meteor falls, 4-heart impact, temporary Sculk crater (auto-reverts).
- Passive — **Warden's Gaze**: 20% chance / 2s Blindness on normal
  (non-ability) crossbow shots only.

**Frost Blade**
- Ability 1 — **Frozen Barrage** (F): brief levitate + charge, then 4
  homing ice projectiles at nearby players, 2 hearts each. 70s cooldown.
- Ability 2 — **Ice Armor** (Shift+F): 4s full damage immunity + Glowing,
  blocked hits play an icy crack.

**Leviathan's Fang**
- Ability 1 — **Leviathan Tsunami** (F): a *real* moving wall of water
  (placed a slice at a time and reverted a couple ticks later, so it never
  permanently floods anything) that carries and repeatedly ticks anyone
  caught inside for 0.5 hearts per hit.
- Ability 2 — **Tidal Dash** (Shift+F): a fast water-carried dash in
  whatever direction you're looking.

**Void Blade**
- Ability 1 — **Void Slam** (F, twice): first press launches you up inside
  four rotating void rings; second press slams you toward whatever you're
  looking at for a 4-heart AoE crater slam. Auto-cancels safely if you
  never press the second time.
- Ability 2 — **Black Rift** (Shift+F): locks onto your target, hits them
  with a rapid flurry of slashes from randomized directions, then one much
  larger finishing strike after a beat of pause.

**The Eyes** *(new weapon)*
- Ability 1 — **Hollow Purple** (F): rise 7 blocks, a red and a blue sphere
  spiral together and merge into one purple sphere, then fire it as a
  single massive projectile with a small AoE on impact.
- Ability 2 — **Red Beam** (Shift+F): instant 20-block beam, 4-heart
  damage, shatters blocks along its path (auto-reverts).

## Cooldowns & boss bars

Every ability's cooldown lives in `config.yml` under `abilities.<key>.
cooldown-seconds`. Numbers your spec gave explicitly are marked
`# EXPLICIT` in the file; everything else is marked `# ESTIMATE` because
your new ability list didn't specify it — all safe to retune, nothing is
hardcoded in Java.

Boss bars are on by default (`boss-bar.enabled: true`) — every player gets
a live counting-down bar per ability while it's cooling down, colored per
`boss-bar.colors` in config, stacking if more than one ability is on
cooldown at once.

## Trust system

`/trust add|remove|list <player>` still works exactly like before — a
trusted player is exempt from your area-effect abilities (Sculk Meteor,
Frozen Barrage impacts, Leviathan Tsunami, Void Slam, Black Rift, Hollow
Purple), never from your own deliberate single-target hits.

## Commands

| Command | Does |
|---|---|
| `/ability <1\|2>` | Trigger a Warden Crossbow ability without the physical input |
| `/waterability <1\|2>` | Same, for Leviathan's Fang |
| `/frostability <1\|2>` | Same, for Frost Blade |
| `/voidability <1\|2>` | Same, for Void Blade |
| `/eyesability <1\|2>` | Same, for The Eyes |
| `/warden go` | Give yourself the Warden Crossbow (needs `warden.use`) |
| `/voidreaver` | Give yourself the Void Blade |
| `/frostblade` | Give yourself the Frost Blade |
| `/leviathan` | Give yourself Leviathan's Fang |
| `/eyes` | Give yourself The Eyes |
| `/frostgui` | (kept as-is from your original plugin.yml — gives the Warden Crossbow) |
| `/onuxmenu` | Opens the arsenal GUI — now with all five weapons |
| `/trust <add\|remove\|list> [player]` | Manage your trust list |
| `/cooldown` | Reset all of your own ability cooldowns |

## Identification

Every weapon is tagged with a `PersistentDataContainer` string under
`onyx:weapon_id` (see `weapons/WeaponType.java` /
`weapons/WeaponFactory.java`) — never display name or lore. Rename an item,
re-lore it, run it through an anvil, whatever — its abilities keep working.

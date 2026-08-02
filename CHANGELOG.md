# Changelog

## [1.0.0] - 2026-08-02

### Added
- Rewarded ads: optional "Watch Ad" for 2x offline earnings, daily bonus, and boss loot
- Remove-ads IAP: one-time purchase gives ad-free experience with all bonuses unlocked
- Boss reward popup every 100 floors with bonus loot offer
- CHANGELOG.md

### Changed
- Version bump to 1.0.0 for Play Store release
- SettingsButton now shows version from BuildConfig
- OfflinePopup and DailyRewardPopup support ad-powered bonus options

## [0.4.0] - 2026-08-01

### Added
- Achievement system with 5 tiered achievements and stat bonuses
- Achievement panel (trophy button) with progress bars
- Stats panel showing lifetime stats (enemies, bosses, floors, rebirths, gear, play time)
- Daily login rewards cycling through 7-day rewards
- Tutorial tooltips for hero leveling, forge, and rebirth
- Reset progress option with double-confirm in settings

### Fixed
- Achievement stat key mismatch (bosses_slain -> bosses_killed)
- heroes_unlocked stat now tracked on hero unlock

## [0.3.0] - 2026-07-31

### Added
- Prestige/Rebirth system: rebirth at floor 50+ for Ember currency
- Ember upgrade shop with tier-gated upgrades
- Ember multipliers for gold, damage, HP, offline efficiency, starting floor, hero slots
- Rebirth visual effects (screen flash, particles, floating text)

## [0.2.0] - 2026-07-30

### Added
- Forge system: craft gear from gold and materials
- Gear inventory with salvage
- Equip/unequip gear on heroes with stat bonuses
- 5 rarity tiers (Common through Legendary)
- Material drops from enemies

## [0.1.0] - 2026-07-29

### Added
- Initial release: auto-battle idle dungeon RPG
- 6 elemental heroes with unique roles (tank, DPS, support, healer)
- Procedural enemy scaling with boss encounters every 10 floors
- Hero leveling with gold economy
- Offline earnings simulation
- SurfaceView canvas renderer with procedural audio
- Compose overlay UI (hero tray, level-up panel, settings)
- Room database persistence
- GitHub Actions CI (unit tests, lint, APK, AAB)

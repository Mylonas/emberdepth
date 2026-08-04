# Shipping EmberDepth to Google Play

Two halves: the ads and billing are wired up and verified in CI, and the Play
Console work is yours. This is the exact order to do it in.

**Status of the code:** AdMob rewarded ads (3 placements) and a remove-ads IAP
are integrated and building green. They currently serve **Google's official test
ads** — the build falls back to test IDs whenever real ones are not supplied, and
debug builds are pinned to test IDs no matter what. Nothing is live until you do
step 1.

---

## 1. AdMob

1. Sign in at [apps.admob.com](https://apps.admob.com), then **Apps -> Add app
   -> Android -> No** (not yet on Play). Name it `EmberDepth`.
2. Copy the **App ID**: `ca-app-pub-XXXXXXXX~YYYYYYYY` (tilde).
3. **Ad units -> Add ad unit -> Rewarded**, named something like
   `EmberDepth - rewarded`. Copy the **Ad unit ID**:
   `ca-app-pub-XXXXXXXX/ZZZZZZZZ` (slash). The tilde one and the slash one are
   different things, and swapping them is the classic first mistake.
4. Put both in your **user-level** Gradle properties, not in this repo —
   `C:\Users\<you>\.gradle\gradle.properties`:

   ```properties
   ADMOB_APP_ID=ca-app-pub-XXXXXXXX~YYYYYYYY
   ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXX/ZZZZZZZZ
   ```

   The build reads these and bakes them into release builds only. They never
   enter git.
5. Once the app is live, come back and **link the AdMob app to Play**
   (AdMob -> App settings -> Link to Play). Without it you lose a chunk of ad
   revenue and all the Play-side metrics.

> **Never click your own live ads, including "just to check".** It is the
> quickest route to a permanently suspended AdMob account. That is exactly why
> debug builds here cannot serve anything but test ads.

## 2. A signing key

### Option A: build locally

Play needs a signed AAB. Google holds the real app signing key (Play App
Signing); you hold an **upload key**.

You need a JDK for `keytool`. Installing Android Studio gives you one.

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Then create `keystore.properties` in the repo root (already gitignored):

```properties
storeFile=../upload-keystore.jks
storePassword=<yours>
keyAlias=upload
keyPassword=<yours>
```

`app/build.gradle.kts` picks this up automatically and signs release builds.

### Option B: sign in CI, no local toolchain

You have `openssl` (it ships with Git for Windows) but no JDK. This path makes
the keystore with openssl and lets GitHub Actions do the signing.

**The same upload key signs all your games.** If you already made one for the
arcade games, `make-upload-key.sh` will detect it and reuse it.

| Secret | Shared across games? |
| --- | --- |
| `KEYSTORE_BASE64` | shared |
| `KEYSTORE_PASSWORD` | shared |
| `KEY_ALIAS` | shared (`upload`) |
| `KEY_PASSWORD` | shared |
| `ADMOB_APP_ID` | **per game** |
| `ADMOB_REWARDED_ID` | **per game** |

**1. Create the upload key** (skip if you already have one from the arcade
games). In **Git Bash**:

```bash
bash store/make-upload-key.sh
```

It asks for a password twice. Nothing appears as you type. Output lands in
`~/play-upload-key/`. If the key already exists it says so and exits.

**2. Push the secrets:**

```bash
bash store/push-secrets.sh
```

It asks for the keystore password and the two AdMob ids, then sets all six
secrets. Verify with `gh secret list --repo Mylonas/emberdepth`.

**3. Run the workflow.** Actions -> **Signed release bundle** -> *Run workflow*:

```bash
gh workflow run release.yml --repo Mylonas/emberdepth -f versionCode=2 -f versionName=1.0.0
```

The build fails if the signing key is missing, fails if the AdMob ids are still
test ones, and runs `apksigner` to prove the output is not debug-signed. Download
the artifact — the `.aab` is what you upload to Play. Keep `mapping.txt` and
upload it too, so Play can deobfuscate crash reports.

## 3. Play Billing setup

Before the remove-ads purchase works in production:

1. **Play Console -> Monetize -> In-app products -> Create product**
2. Product ID: `remove_ads` (must match the code exactly)
3. Name: "Remove Ads", Description: "Remove all ads and get bonus rewards automatically"
4. Set your price
5. **Activate** the product

The app uses Play Billing Library v7. Purchases are non-consumable and tied to
the user's Google account, so they survive reinstalls and device changes.

## 4. Play Console

**Create the app** at [play.google.com/console](https://play.google.com/console).
App name `EmberDepth`, free, it is a **game**, category **Role Playing**, package
`com.mikmy.emberdepth`.

**Store listing** — all required before you can submit:

| Asset | Requirement |
| --- | --- |
| App icon | 512x512 PNG, 32-bit, under 1 MB |
| Feature graphic | 1024x500 PNG or JPEG |
| Phone screenshots | 2 to 8, at least 320px on the short side |
| Short description | 80 characters |
| Full description | 4000 characters |

Suggested short description:

> Idle dungeon RPG. Build your party. Forge gear. Rebirth for power.

**Content rating** — fill in the questionnaire. This game has fantasy violence
(auto-combat), no user-generated content. It has in-app purchases (remove ads).

**Ads declaration** — *Yes, my app contains ads*. Not optional.

**Data safety** — with AdMob and Play Billing integrated:

- Does your app collect or share user data? **Yes**
- Data type: **Device or other IDs -> Advertising ID**, collected *and* shared,
  purpose **Advertising or marketing**
- Data type: **Financial info -> Purchase history**, collected, purpose
  **App functionality** (remove-ads state)
- Not processed ephemerally, not user-deletable
- Encrypted in transit: **Yes**

Check against
[AdMob's data disclosure guidance](https://support.google.com/admob/answer/11221321).

**Privacy policy** — required because the app collects the advertising ID.
`PRIVACY.md` here is ready. Host it: push to a public repo, enable
**Settings -> Pages**, use the URL. Or use any static host.

**Target audience** — 13+ keeps you out of the Families policy programme.

**App access** — no login required.

## 5. The 12-testers rule

If your developer account is a **personal** account created after
**13 November 2023**, you cannot publish straight to production. You must run a
**closed test with at least 12 testers opted in for 14 consecutive days**, then
apply for production access.

- "Opted in" means they accepted the invite *and installed the app*.
- The 14 days must be unbroken and most recent at the moment you apply.
- Organisation accounts and older personal accounts are exempt.

Check which kind of account you have before planning a launch date.

## 6. Release

1. **Testing -> Internal testing** first. Upload the AAB, add yourself, install
   from the opt-in link, and confirm: the game runs, rewarded ads appear when you
   tap "Watch Ad", the consent dialog appears in the EEA, and "Remove Ads" in
   settings launches the purchase flow.
2. Then **Closed testing** for the 12-tester run, if step 5 applies.
3. Then **Production**. First review usually takes a few days.

## Gotchas

- **`versionCode` must increase** on every upload. The same number is rejected.
- **targetSdk 35** is already set; Play enforces a recent target API.
- The app will show ads to *you* in production. Do not tap them.
- If no rewarded ad appears, the SDK might still be loading — the "Watch Ad"
  button is disabled until `adReady` is true. Frequency caps in AdMob control how
  often rewarded ads are available.
- A missing `com.google.android.gms.ads.APPLICATION_ID` meta-data crashes on
  launch. It is already in the manifest, wired to the Gradle property.
- The `remove_ads` product must be **activated** in Play Console before purchase
  works. An inactive product returns `ITEM_UNAVAILABLE`.

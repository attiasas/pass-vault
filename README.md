# PassVault – Android auth vault (Java)

A minimal, local-only Android app to store and manage auth entries (passwords/tokens) protected by a master password. All data is stored encrypted on device.

## Features

- **Master password** – Log in with one password; can be changed (re-encrypts all data).
- **Encrypted storage** – Data encrypted with salt + master password (AES-256-GCM or AES-256-CBC).
- **Auth entries** – Add, edit, delete entries (title, username, password/token).
- **Masked secrets** – Password/token shown as `••••••••`; tap **Show** to reveal, **Hide** to mask.
- **Health badge** – Per-entry status (Good / Fair / Low / Stale) from time since last update.
- **More info** – Per entry: health score, strength, start/update dates, days in use, and **history** of previous passwords (start/end date, days used, value).
- **Password generator** – Length, digits/uppercase/special, lowercase ratio; copy to clipboard or use when adding/editing an entry.
- **Encryption method** – Choose AES-256-GCM or AES-256-CBC in Settings.
- **Storage backend** – Choose file (.dat) or SQL database; switch in Settings (data is migrated).
- **Export** – JSON file of all current entries (no history).
- **Import** – Restore from an exported JSON file (e.g. move to another device).

## Project structure (maintainable, separated)

```
app/src/main/java/com/passvault/app/
├── PassVaultApp.java              # Application; holds VaultRepository
├── crypto/
│   ├── KeyDerivation.java         # PBKDF2 key + verification hash from master + salt
│   └── VaultCipher.java           # AES-GCM / AES-CBC encrypt/decrypt
├── data/
│   ├── AuthEntry.java             # Single entry + history
│   ├── EntryHistoryItem.java     # One past password record
│   └── EncryptionMethod.java     # AES_256_GCM, AES_256_CBC
├── storage/
│   ├── VaultStorage.java          # Interface: load/save encrypted entries
│   ├── FileVaultStorage.java      # File-backed storage (.dat)
│   ├── SqlVaultStorage.java       # SQLite-backed storage
│   ├── StorageType.java           # FILE / SQL (user choice in Settings)
│   ├── PrefsManager.java          # Salt, master hash, encryption method, storage type
│   └── VaultRepository.java       # Unlock, CRUD; delegates to current VaultStorage
├── ui/
│   ├── LoginActivity.java
│   ├── VaultActivity.java         # List of entries + FAB
│   ├── EntriesAdapter.java       # Entry cards, health badge, show/hide, more info
│   ├── AddEditEntryActivity.java
│   ├── PasswordGeneratorActivity.java
│   ├── SettingsActivity.java     # Encryption, storage type, change password, export/import
│   └── MoreInfoActivity.java     # Health, strength, dates, history list
└── util/
    ├── ExportImport.java          # JSON export/import (no history)
    ├── HealthCalculator.java      # Health score from last update time
    ├── PasswordStrength.java     # Strength 0–100 + label
    ├── PasswordGenerator.java    # Configurable random password
```

## Build and run

- **Android Studio** – Open the project; sync Gradle and run on a device or emulator.
- **Command line** – Ensure you have the Gradle wrapper (e.g. run `gradle wrapper` once if needed), then:
  ```bash
  ./gradlew assembleDebug
  ```
  Install the APK from `app/build/outputs/apk/debug/`.

## Security notes

- Master password is never stored; only a PBKDF2-derived hash (with salt) is kept for verification.
- Vault file is encrypted with a key derived from the master password and salt.
- Changing the master password decrypts with the old key and re-encrypts with the new one; all data is updated.
- Export produces plain JSON (current entries only); store and transfer export files carefully.

## Design

- Modern, minimal UI: primary blue, cards for entries, health badges (green/amber/red), clear typography.
- Only necessary info on screen; details (history, exact health number, strength) in **More info** per entry.

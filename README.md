# ⚔️ RPG Battle System (Turn-Based GUI Game)

Proyek ini adalah implementasi game **RPG Battle System berbasis Turn-Based** yang dikembangkan menggunakan **Java Plain (Desktop Swing)** dengan penerapan maksimal konsep dan prinsip Pemrograman Berorientasi Objek (PBO/OOP). 

Desain dan cara kerja antarmuka (GUI) game ini dirancang menyerupai dan mengadopsi mekanisme dari prototipe web [rpg_battle_prototype.html](file:///c:/vscd/SMT%204/pebeo/rpg_battle_prototype.html).

---

## 🎨 Rancangan Antarmuka (GUI) Game

Desain GUI game dirancang agar modern, responsif, dan bernuansa gelap (*dark mode*) menggunakan komponen visual bawaan Java Swing yang dimodifikasi. Game ini memiliki 3 layar utama yang dikontrol melalui `CardLayout`:

```
               ┌──────────────────────────┐
               │    Layar Pilihan Hero    │
               │ (Warrior, Mage, Rogue)   │
               └────────────┬─────────────┘
                            │ (Tombol Pilih Musuh)
                            ▼
               ┌──────────────────────────┐
               │   Layar Pemilihan Musuh  │
               │  (Goblin, Orc, dll +     │
               │   Difficulty Multiplier) │
               └────────────┬─────────────┘
                            │ (Tombol Mulai Battle)
                            ▼
               ┌──────────────────────────┐
               │    Layar Battle Arena    │
               │ (Fighter Stats, Actions, │
               │ Log HTML, Overlay Modal) │
               └──────────────────────────┘
```

### 1. Layar Pemilihan Hero (`heroScreen`)
- Menampilkan 3 kartu pilihan Hero (Arin - Warrior, Lyra - Mage, Zhen - Rogue) lengkap dengan ikon emoji, kelas, dan grafik statistik HP, ATK, DEF dasar.
- Memiliki selector **Tingkat Kesulitan** (Mudah: multiplier `0.7x`, Normal: `1.0x`, Keras: `1.4x`) yang memengaruhi stat HP dan ATK dari musuh.
- Tombol "Pilih Musuh" akan aktif setelah salah satu kartu hero diklik.

### 2. Layar Pemilihan Musuh (`enemyScreen`)
- Menampilkan 4 musuh yang bisa ditantang (Goblin King, Orc Berserker, Dark Dragon, Lich Lord) dengan informasi stat dasar dan tingkat ancaman.
- Tombol navigasi "Kembali" untuk merubah hero/kesulitan dan tombol "Mulai Battle!" untuk masuk ke arena.

### 3. Layar Battle Arena (`battleScreen`)
- **Fighter Status**: Bagian atas menampilkan info nama, kelas, progress bar HP (hijau), progress bar MP (biru - hanya untuk hero), dan status effect aktif (berupa tag warna).
- **Turn Indicator**: Menampilkan penanda giliran yang aktif (Giliran Hero berwarna emas, Giliran Musuh berwarna merah).
- **Action Panel**: Menyediakan 4 tombol aksi utama:
  1. `Serang`: Melancarkan serangan fisik biasa tanpa biaya MP.
  2. `Skill`: Membuka modal pop-up pilihan skill sesuai dengan `Job` hero. Membutuhkan MP.
  3. `Item`: Membuka modal pop-up pilihan item habis pakai (*Health Potion*, *Mana Elixir*, *Fire Bomb*) dari inventaris.
  4. `Bertahan`: Meningkatkan pertahanan (DEF x1.5) untuk ronde tersebut.
- **HTML Battle Log**: Log pertempuran interaktif menggunakan `JTextPane` dengan format HTML sehingga baris tindakan memiliki warna khusus (Merah untuk damage hero, Oranye untuk serangan musuh, Ungu untuk skill, Hijau untuk healing, dan Biru untuk informasi sistem).
- **Modal Overlays**: Menggunakan `JLayeredPane` untuk menampilkan panel pop-up transparan di atas arena pertempuran (untuk memilih Skill/Item dan menampilkan hasil akhir pertandingan Win/Lose lengkap dengan statistik pertarungan).

---

## 🛠️ Hubungan Kelas & Struktur OOP (Class Diagram)

Untuk menghubungkan `Character.java`, `Skill.java`, dan `Job.java`, kami menggunakan prinsip **Inheritance (Pewarisan)** dan **Composition (Komposisi)**:

```
                  ┌──────────────────────┐
                  │      Character       │ <─── Abstract Class
                  └──────────┬───────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   ┌────────────────┐                ┌────────────────┐
   │      Hero      │                │     Enemy      │
   └───────┬────────┘                └────────┬───────┘
           │ (Has-A / Komposisi)              │ (Has-A)
           ▼                                  ▼
   ┌────────────────┐                ┌────────────────┐
   │      Job       │                │     Skill      │ <─── Abstract Class
   └───────┬────────┘                └────────┬───────┘
           │ (Has-Many)                       │
           ▼          ┌───────────────┬───────┴───────┬───────────────┐
   ┌────────────────┐ │               │               │               │
   │     Skill      │ ▼               ▼               ▼               ▼
   └────────────────┘ DamageSkill     HealSkill       BuffSkill       StatusSkill
```

1. **`Character` (Abstract Class)**: Menyediakan atribut umum (`name`, `hp`, `mp`, `attack`, `defence`, `speed`) dan logika dasar (setter HP/MP dengan validasi rentang nilai). Memiliki metode abstrak `attack` dan `takeTurn`.
2. **`Hero` & `Enemy` (Inheritance)**: Mewarisi `Character`.
   - `Hero` memiliki variabel `Job` (Warrior/Mage/Rogue) dan list `Item` (Komposisi).
   - `Enemy` memiliki atribut `difficulty` dan AI sederhana dalam menentukan aksinya.
3. **`Job` (Class)**: Berperan menghubungkan `Hero` dengan daftar `Skill`-nya. Setiap `Hero` memiliki satu `Job` (Komposisi), dan setiap `Job` menyimpan kumpulan `Skill` yang sesuai dengan peran kelas tersebut.
4. **`Skill` (Abstract Class & Polymorphism)**: Memiliki metode abstrak `use(Character caster, Character target, BattleSystem system)`. Berbagai jenis skill diturunkan menjadi subclass (`DamageSkill`, `HealSkill`, `BuffSkill`, `StatusSkill`) dan mengimplementasikan efek uniknya secara polimorfis tanpa membutuhkan percabangan string yang rumit.
5. **`Item` (Class)**: Merepresentasikan item yang dapat dikonsumsi oleh `Hero` dari inventarisnya.
6. **`StatusEffect` (Class & Enum)**: Merepresentasikan efek status berkala (*Poison*, *Burn*, *Stun*, *Evade*, *Buff*, *Berserk*) yang mempengaruhi aksi karakter pada giliran bertarungnya.
7. **`BattleSystem` (Controller/Singleton)**: Mengendalikan aliran turn-based RPG, validasi pertempuran, dan pencatatan statistik permainan.
8. **`GameGUI` (Viewer/Listener)**: Menangani render tampilan grafis desktop dan menangkap input user.

---

## 🚀 Cara Menjalankan Aplikasi

Aplikasi dikembangkan menggunakan Java murni (*plain Java*) tanpa library eksternal, sehingga Anda hanya memerlukan JDK (Java Development Kit) terinstal pada komputer Anda.

### 1. Kompilasi Program
Buka terminal/PowerShell pada direktori proyek, lalu jalankan perintah kompilasi berikut:
```bash
javac -encoding utf-8 -d bin src/*.java
```

### 2. Jalankan Program
Setelah kompilasi sukses, jalankan kelas utama `Main` dengan merujuk ke folder output `bin` menggunakan classpath (`-cp`):
```bash
java -cp bin Main
```


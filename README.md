# Tucil 2 Strategi Algoritma - Kompresi Gambar dengan Quadtree

## a. Penjelasan Singkat

Program ini digunakan untuk melakukan kompresi gambar menggunakan struktur data **Quadtree** dan metode evaluasi homogenitas seperti:

- Variance
- Mean Absolute Deviation (MAD)
- Max Pixel Difference (MPD)
- Entropy

Program akan membaca sebuah gambar masukan, melakukan segmentasi berdasarkan metode yang dipilih, dan menghasilkan gambar hasil kompresi serta statistik seperti waktu eksekusi, ukuran file, dan kedalaman pohon.

## b. Requirement dan Instalasi

- Java JDK 17 atau versi yang lebih baru
- Sistem operasi Windows atau Linux (WSL juga didukung)

### Instalasi Java:

**Windows**: Unduh JDK dari: [https://jdk.java.net](https://jdk.java.net) atau [https://adoptium.net](https://adoptium.net)

**Linux**:

```bash
sudo apt install openjdk-17-jdk
```

Pastikan instalasi berhasil:

```bash
java -version
```

## c. Cara Kompilasi

Tersedia dua skrip kompilasi otomatis berdasarkan sistem operasi:

### Windows:

```cmd
build.bat
```

### Linux / WSL:

```bash
chmod +x build.sh  # hanya pertama kali
./build.sh
```

## d. Cara Menjalankan Program

Program dapat dijalankan dengan skrip berikut:

### Windows:

```cmd
run.bat
```

### Linux / WSL:

```bash
./run.sh
```

Program akan meminta input berupa:

1. Ambang batas (threshold)
2. Ukuran blok minimum
3. Lokasi file output hasil kompresi

Hasil kompresi akan disimpan dan informasi statistik ditampilkan di terminal.

### Membersihkan file hasil kompilasi

**Windows:**

```cmd
clean.bat
```

**Linux/WSL:**

```bash
./clean.sh
```

## e. Author

Nama: Nathanael Rachmat\
NIM: 13523142\
Kelas: IF2211 - Strategi Algoritma

---

Dokumen ini merupakan bagian dari tugas kecil 2 tahun 2025 di Sekolah Teknik Elektro dan Informatika, Institut Teknologi Bandung.

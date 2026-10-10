# Remiyu Backend

Backend Java 21 + Spring Boot 3.5.16 untuk Flutter. PostgreSQL menyimpan akun, Workspace, keanggotaan, pesan sumber, tugas, agenda, dan riwayat import. AI sementara dipanggil melalui HTTP ke Mockoon.

## Arsitektur

Flutter → Spring Boot (8081) → PostgreSQL
                         → Mockoon (3002), simulasi endpoint FastAPI

Spring Boot tetap memanggil kontrak `POST /v1/extractions`. FastAPI yang memilih OpenAI/model lokal. `AI_PROVIDER` hanya label provider pada riwayat import; mengganti label ini saja tidak mengubah endpoint.

## Jalankan lokal

Prasyarat: JDK 21, Maven 3.9+, Node.js/npm untuk Mockoon CLI, dan PostgreSQL. Database `remiyu` sudah dibuat pada mesin development saat implementasi. Pada mesin baru, buat database kosong terlebih dahulu.

Default backend:
- Backend: http://localhost:8081
- Mockoon: http://localhost:3002
- Database lokal profile: jdbc:postgresql://fajris-mac-mini.tail2fab46.ts.net:5432/remiyu
- Username lokal: postgres (role `postgre` tidak ditemukan pada mesin ini)
- Password lokal mengikuti konfigurasi development pada `.env.example`

Terminal pertama, dari folder backend:

```powershell
.\scripts\start-mockoon.ps1
```

Atau buka file `mockoon/remiyu-ai.json` melalui menu **File → Open local file** pada Mockoon Desktop, kemudian jalankan environment di port 3002. Environment Mockoon lain tidak perlu diubah.

Terminal kedua:

```powershell
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = 'postgres'
.\scripts\start-backend.ps1
```

## Environment profile

Profile dipilih eksplisit melalui script, atau dengan `SPRING_PROFILES_ACTIVE` saat menjalankan JAR. File `.env.*.example` adalah template; jangan commit file `.env` yang berisi password/API key.

| Profile | AI gateway default | Database dan CORS |
|---|---|---|
| `local` | Mockoon: `http://localhost:3002` | Default PostgreSQL lokal dan CORS localhost |
| `dev` | FastAPI: `http://fajris-mac-mini.tail2fab46.ts.net/` | Wajib isi `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ORIGINS` |
| `prod` | FastAPI: `http://fajris-mac-mini.tail2fab46.ts.net/` | Wajib isi `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ORIGINS`; seeder dimatikan |

Contoh menjalankan:

```powershell
# Mockoon + database lokal
.\scripts\start-backend.ps1 -Profile local

# Development atau production: ekspor secret sebelum menjalankan.
$env:DB_URL = 'jdbc:postgresql://host:5432/remiyu'
$env:DB_USERNAME = 'username'
$env:DB_PASSWORD = 'password'
$env:CORS_ORIGINS = 'https://frontend.example.com'
.\scripts\start-backend.ps1 -Profile dev
```

Untuk menjalankan JAR, gunakan `SPRING_PROFILES_ACTIVE=dev` atau `prod`. Endpoint Tailscale memakai HTTP sesuai konfigurasi yang diminta; pastikan host backend dapat merutekan dan mempercayai jaringan Tailscale tersebut sebelum deployment.

File `.env.example` adalah contoh konfigurasi; Maven/PowerShell tidak membacanya otomatis. Ekspor variabel environment atau konfigurasi Run Configuration IDE. Flyway membuat tabel otomatis saat aplikasi mulai.

Pada mesin Windows ini Java/npm sempat gagal memverifikasi sertifikat registry. Script menyediakan opsi menggunakan sertifikat sistem Windows/Node, tanpa mematikan verifikasi TLS:

```powershell
.\scripts\start-mockoon.ps1 -UseWindowsTrust
.\scripts\start-backend.ps1 -UseWindowsTrust
```

Node 22.20.0 pada mesin ini mendukung `--use-system-ca`.

## Data demo lokal

Data demo kini berupa migration SQL Flyway `V3__local_demo_seed.sql`, bukan seeder Java. Migration ini hanya memasukkan data jika profile `local` aktif. Profile `dev`, `prod`, dan test menjalankan migration yang sama dengan `seed_enabled=false`, sehingga tidak menerima data demo.

Pada `local`, data demo aktif secara default. Untuk menonaktifkannya sebelum database baru pertama kali dijalankan:

```powershell
$env:SEED_ENABLED = 'false'
.\scripts\start-backend.ps1 -UseWindowsTrust
```

Data demo berisi dua akun terisolasi dengan password sama, `RemiyuDemo2026!`: `demo@remiyu.local` untuk Workspace operasional dan `nadia.demo@remiyu.local` untuk Workspace konten Telegram. Akun kedua memiliki import, pesan sumber, tiga tugas, dan satu agenda sendiri untuk menguji isolasi data per pengguna. Setiap request dari backend ke AI (import, ringkasan, dan deteksi rutinitas) menyertakan `userId` pengguna yang sudah terautentikasi; ID tersebut tidak berasal dari Flutter dan tidak ditampilkan ke pengguna. Data ini tidak memanggil AI dan tidak menagih pembayaran. Flyway menjalankan tiap migration hanya sekali per database; ubah database/riwayat migration atau gunakan database lokal baru bila perlu menerapkan ulang data demo.

## Struktur kode

```text
src/main/java/id/remiyu/backend/
  controller/       endpoint HTTP, validasi request
  service/          aturan bisnis, akses Workspace, transaksi
  repository/       query SQL melalui JdbcClient
  dto/
    request/        request API tervalidasi
    response/       response API tanpa data sensitif akun
    ai/             kontrak gateway AI
  model/            record internal akun dan pesan hasil parsing
  integration/ai/   HTTP client Mockoon/FastAPI
  security/         bearer token filter
  config/           konfigurasi Spring Security dan CORS
  support/importchat/ parser WhatsApp dan pembaca file
  common/
    exception/      exception dan handler error API
    util/           hashing
src/main/resources/db/migration/       migration lintas database
src/main/resources/db/postgresql-migration/  migration khusus PostgreSQL, misalnya pgvector/RAG
mockoon/
docs/
scripts/
```

Controller hanya menangani HTTP/validasi dan memanggil Service. Service menangani aturan bisnis/akses dan transaksi; Repository menangani SQL melalui Spring JdbcClient. DTO request/response berada di package sendiri, sehingga Service tidak bergantung pada Controller. Record model internal tidak dipakai langsung sebagai response akun. Migration Flyway menjadi sumber schema database.

## Status kerja dan review

Status kerja menjelaskan progres operasional data. `review_status` terpisah untuk menunjukkan apakah hasil ekstraksi AI sudah diperiksa pengguna. Migration `V8__review_status.sql` memberi nilai awal `PENDING` untuk data lama dan baru.

| Data | status | review_status |
|---|---|---|
| Task | TODO, IN_PROGRESS, DONE | PENDING, APPROVED, REJECTED |
| Agenda | CONFIRMED, CANCELLED | PENDING, APPROVED, REJECTED |
| Routine | ACTIVE, PAUSED | PENDING, APPROVED, REJECTED |

## Fondasi RAG

Migration PostgreSQL `V6__rag_pgvector.sql` menyiapkan extension `pgvector` dan tabel `rag_chunks`. Tabel ini belum dipakai endpoint aplikasi: ia hanya menyiapkan penyimpanan chunk, metadata, dan embedding untuk RAG berikutnya. Setiap chunk wajib memiliki `user_id` dan `workspace_id`, sehingga retrieval nantinya selalu dapat dibatasi ke pemilik dan Workspace aktif. Dimensi `vector(1536)` ditetapkan untuk `text-embedding-3-small`; mengganti model berdimensi lain membutuhkan re-embedding/migration baru.

### Instal pgvector pada PostgreSQL Windows

Migration RAG sengaja opt-in: server PostgreSQL harus memasang binary `pgvector` terlebih dahulu. `CREATE EXTENSION` melalui Flyway hanya **mengaktifkan** extension pada database; ia tidak dapat memasang file `vector.dll` dan `vector.control` ke server PostgreSQL.

Panduan berikut untuk PostgreSQL 16 pada Windows. Jalankan build dengan **x64 Native Tools Command Prompt for Visual Studio** yang sudah memiliki workload **Desktop development with C++** dan **Windows 10/11 SDK**. SDK diperlukan agar header `corecrt.h` tersedia.

1. Pastikan versi server dan tentukan `PGROOT`. Versi binary pgvector harus mengikuti instalasi PostgreSQL yang menjalankan database `remiyu`, bukan versi Visual Studio.

```cmd
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d remiyu -c "SHOW server_version;"
set "PGROOT=C:\Program Files\PostgreSQL\16"
```

2. Clone source pgvector dan build. Contoh ini memakai versi `0.8.7`.

```cmd
git clone --branch v0.8.7 https://github.com/pgvector/pgvector.git C:\Users\%USERNAME%\Projects\pgvector
cd /d C:\Users\%USERNAME%\Projects\pgvector
nmake /F Makefile.win
```

3. Tutup terminal tersebut. Buka kembali **x64 Native Tools Command Prompt** dengan **Run as administrator**, lalu set ulang environment dan install. Hak administrator dibutuhkan karena target berada di `C:\Program Files\PostgreSQL\16`.

```cmd
call "C:\Program Files (x86)\Microsoft Visual Studio\18\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=x64 -host_arch=x64
set "PGROOT=C:\Program Files\PostgreSQL\16"
cd /d C:\Users\%USERNAME%\Projects\pgvector
nmake /F Makefile.win install
```

Jika Visual Studio dipasang dengan edisi atau versi lain, sesuaikan path `VsDevCmd.bat`. Validasi hasil instalasi:

```cmd
dir "C:\Program Files\PostgreSQL\16\lib\vector.dll"
dir "C:\Program Files\PostgreSQL\16\share\extension\vector.control"
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d remiyu -c "SELECT name, default_version, installed_version FROM pg_available_extensions WHERE name = 'vector';"
```

Baris terakhir harus mengembalikan `vector`. Jangan membuat `vector.dll` secara manual atau menghapus `CREATE EXTENSION IF NOT EXISTS vector` dari migration V6.

Setelah package tersedia, aktifkan migration PostgreSQL saat menjalankan backend:

```powershell
$env:FLYWAY_LOCATIONS = 'classpath:db/migration,classpath:db/postgresql-migration'
.\scripts\start-backend.ps1 -Profile local
```

Flyway kemudian menjalankan `CREATE EXTENSION IF NOT EXISTS vector` dan membuat `rag_chunks`. Verifikasi extension aktif pada database `remiyu`:

```powershell
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d remiyu -c "SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';"
```

## Endpoint utama

Semua endpoint selain register, login, dan health memerlukan header `Authorization: Bearer <accessToken>`.

| Method | Path | Fungsi |
|---|---|---|
| GET | /actuator/health | Kesehatan backend/database |
| GET | /api/v1/dashboard | Overview 14 hari, prioritas hari ini, tugas dan agenda terdekat |
| POST | /api/v1/auth/register | Daftar akun |
| POST | /api/v1/auth/login | Login |
| GET | /api/v1/auth/me | Akun aktif |
| POST | /api/v1/auth/logout | Cabut token |
| GET, POST | /api/v1/workspaces | Daftar/buat Workspace |
| GET | /api/v1/workspaces/{id} | Detail Workspace |
| GET | /api/v1/workspaces/{id}/members | Anggota |
| POST | /api/v1/workspaces/{id}/invitation | Token join Workspace untuk admin |
| POST | /api/v1/workspaces/join/{token} | Bergabung ke Workspace Remiyu |
| GET, POST | /api/v1/workspaces/{id}/tasks | Daftar/buat tugas |
| GET, PATCH, DELETE | /api/v1/workspaces/{id}/tasks/{taskId} | Baca/edit/hapus tugas |
| PATCH | /api/v1/workspaces/{id}/tasks/{taskId}/status | TODO / IN_PROGRESS / DONE |
| POST | /api/v1/workspaces/{id}/tasks/{taskId}/confirm | DRAFT → TODO |
| GET, POST | /api/v1/workspaces/{id}/tasks/{taskId}/submissions | Riwayat/kirim hasil tugas multipart `file`, `link`, `note` |
| GET, POST | /api/v1/workspaces/{id}/imports | Riwayat/import multipart `file` |
| GET | /api/v1/workspaces/{id}/messages?ids={uuid,...} | Pesan sumber dalam Workspace |
| GET | /api/v1/workspaces/{id}/agendas | Agenda hasil import |
| PATCH, DELETE | /api/v1/workspaces/{id}/agendas/{agendaId} | Edit/hapus agenda |
| POST | /api/v1/workspaces/{id}/agendas/{agendaId}/confirm | DRAFT → CONFIRMED |

Daftar tugas mendukung `status`, `limit` 1–100, dan `offset`; agenda mendukung `limit`/`offset`. Contoh request tersedia di `docs/requests.http`.

## Postman

Import dua file berikut ke Postman, lalu pilih environment **Remiyu Local**:

- `postman/Remiyu Backend.postman_collection.json`
- `postman/Remiyu Local.postman_environment.json`

Pilih **Auth → Login Admin Demo** atau **Login Nadia Demo**, lalu jalankan **Workspace → Daftar Workspace**. Request menyimpan `accessToken`, `userId`, dan `workspaceId` otomatis. Folder **AI Workspace dan Rutinitas** menguji ringkasan serta deteksi rutinitas; saat Mockoon aktif, kedua akun seeder menerima fixture AI yang berbeda berdasarkan `userId`. Ubah `sampleChatPath` jika lokasi proyek berbeda. Request membuat, mengubah, dan menghapus data ditujukan untuk pengujian manual; jangan jalankan seluruh koleksi pada data penting.

Bearer token berupa token acak 256 bit, hash SHA-256 disimpan di database dengan masa aktif default 24 jam. Password disimpan menggunakan BCrypt. Token bukan JWT. Logout langsung mencabut token. Akses data selalu dibatasi keanggotaan Workspace; pembuatan, import, edit, hapus, dan konfirmasi draft dibatasi admin. Anggota dapat memperbarui status tugas yang sudah dikonfirmasi.

## Alur import yang sudah bekerja

1. Admin memilih Workspace lalu mengunggah `.txt` atau `.zip` berisi tepat satu chat `.txt`.
2. Parser membaca pengirim, timestamp, pesan multiline; format tanggal Indonesia (hari/bulan/tahun) atau ISO.
3. Pesan unik disimpan berdasarkan Workspace + timestamp + pengirim + isi.
4. Hanya pesan baru yang dikirim ke Mockoon.
5. Respons dan source-message IDs divalidasi.
6. Tugas dan agenda disimpan sebagai DRAFT.
7. Admin dapat mengedit dan mengonfirmasi hasil.

Import ulang pesan yang sama tidak membuat tugas/agenda baru dan tidak memanggil AI lagi. Jika HTTP AI gagal atau JSON/sumber tidak valid, transaksi import dibatalkan sehingga file dapat dicoba ulang. ZIP tidak diekstrak ke filesystem.

Batas pilot: upload 1 MB, isi ZIP terurai maksimal 1 MB total, maksimal 100 entri ZIP, 1.000 pesan per import, 16.000 karakter per pesan. Format tanggal month/day belum didukung. Import masih sinkron dengan timeout AI 15 detik; queue/worker terpisah untuk volume besar belum dibuat.

## Status integrasi produk

- Workspace pertama FREE; Workspace berikutnya PAYMENT_REQUIRED.
- Channel selalu NOT_CONNECTED saat dibuat; tidak ada klaim realtime atau transaksi pembayaran berhasil.
- Invitation backend ini untuk keanggotaan Remiyu, bukan link grup WhatsApp/Telegram/Teams.
- Mockoon menghasilkan fixture tugas/agenda untuk menguji alur, bukan hasil AI yang memahami chat.
- Integrasi platform, payment gateway, email verifikasi/reset password, push realtime, serta penggantian model di FastAPI belum diimplementasikan.
- Flutter sudah terhubung untuk auth, Workspace/invitation/join, import/review tugas, agenda, dan unggah hasil tugas. Android emulator menggunakan `http://10.0.2.2:8081`; perangkat fisik memakai IP LAN komputer melalui `API_BASE_URL`. Token disimpan di secure storage jika user memilih Ingat saya.
- Submission menyimpan tautan/catatan dan file maksimal 1 MB di PostgreSQL melalui migration V2; status tugas menjadi DONE dalam transaksi yang sama. Respons tugas memuat `submitted`. Endpoint daftar submission hanya menyediakan metadata, bukan isi file. Object storage, antivirus, download bukti, kuota total penyimpanan, dan idempotency key belum tersedia; ini implementasi pilot.
- Test otomatis hanya mencakup parser chat dan pembaca file import. Pengujian endpoint, repository, PostgreSQL, dan Mockoon dilakukan manual melalui Postman atau smoke test pada environment lokal.

## Build dan test

```powershell
mvn spotless:apply
mvn clean verify
java -jar target/remiyu-backend-0.1.0-SNAPSHOT.jar
```

Untuk trust store Windows pada mesin ini:

```powershell
mvn '-Djavax.net.ssl.trustStoreType=Windows-ROOT' '-Djavax.net.ssl.trustStore=NONE' test
```

Test otomatis tidak memakai database. Jalankan smoke test setelah backend dan Mockoon hidup untuk memverifikasi integrasi PostgreSQL secara manual:

```powershell
.\scripts\smoke-test.ps1
```

## Docker

```powershell
$env:DB_PASSWORD = 'pilih-password-sendiri'
docker compose up --build
```

Compose menyediakan PostgreSQL, Mockoon, dan backend. Database menggunakan volume persisten, Mockoon terikat localhost port 3002, backend port 8081. Docker belum tersedia pada mesin development saat implementasi; Compose disediakan sebagai konfigurasi, bukan hasil deployment yang sudah diuji.

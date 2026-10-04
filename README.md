# Remiyu Backend

Backend Java 21 + Spring Boot 3.5.16 untuk Flutter. PostgreSQL menyimpan akun, Workspace, keanggotaan, pesan sumber, tugas, agenda, dan riwayat import. AI sementara dipanggil melalui HTTP ke Mockoon.

## Arsitektur

Flutter → Spring Boot (8081) → PostgreSQL
                         → Mockoon (3002), simulasi endpoint FastAPI

Nanti ganti `AI_BASE_URL` ke FastAPI. Spring Boot tetap memanggil kontrak `POST /v1/extractions` yang sama. FastAPI yang memilih OpenAI/model lokal. `AI_PROVIDER` hanya label provider pada riwayat import; mengganti label ini saja tidak mengubah endpoint.

## Jalankan lokal

Prasyarat: JDK 21, Maven 3.9+, Node.js/npm untuk Mockoon CLI, dan PostgreSQL. Database `remiyu` sudah dibuat pada mesin development saat implementasi. Pada mesin baru, buat database kosong terlebih dahulu.

Default backend:
- Backend: http://localhost:8081
- Mockoon: http://localhost:3002
- Database: jdbc:postgresql://localhost:5432/remiyu
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
$env:DB_PASSWORD = 'postgre'
.\scripts\start-backend.ps1
```

File `.env.example` adalah contoh konfigurasi; Maven/PowerShell tidak membacanya otomatis. Ekspor variabel environment atau konfigurasi Run Configuration IDE. Flyway membuat tabel otomatis saat aplikasi mulai.

Pada mesin Windows ini Java/npm sempat gagal memverifikasi sertifikat registry. Script menyediakan opsi menggunakan sertifikat sistem Windows/Node, tanpa mematikan verifikasi TLS:

```powershell
.\scripts\start-mockoon.ps1 -UseWindowsTrust
.\scripts\start-backend.ps1 -UseWindowsTrust
```

Node 22.20.0 pada mesin ini mendukung `--use-system-ca`.

## Seeder development

Seeder opsional, idempotent, dan default mati. Aktifkan sebelum menjalankan backend:

```powershell
$env:SEED_ENABLED = 'true'
$env:SEED_EMAIL = 'demo@remiyu.local'
$env:SEED_PASSWORD = 'RemiyuDemo2026!'
.\scripts\start-backend.ps1 -UseWindowsTrust
```

Data seed: satu akun admin, satu Workspace demo, dan satu tugas manual. Gunakan password pilihan sendiri jika dipakai di luar development. Seeder tidak memanggil AI, tidak mengaktifkan realtime, dan tidak menagih pembayaran.

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
  seeder/           inisialisasi data development
  support/importchat/ parser WhatsApp dan pembaca file
  common/
    exception/      exception dan handler error API
    util/           hashing
src/main/resources/db/migration/
mockoon/
docs/
scripts/
```

Controller hanya menangani HTTP/validasi dan memanggil Service. Service menangani aturan bisnis/akses dan transaksi; Repository menangani SQL melalui Spring JdbcClient. DTO request/response berada di package sendiri, sehingga Service tidak bergantung pada Controller. Record model internal tidak dipakai langsung sebagai response akun. Migration Flyway menjadi sumber schema database.

## Endpoint utama

Semua endpoint selain register, login, dan health memerlukan header `Authorization: Bearer <accessToken>`.

| Method | Path | Fungsi |
|---|---|---|
| GET | /actuator/health | Kesehatan backend/database |
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
- Uji build backend: 14 test lulus. Uji live repository Flutter ke PostgreSQL/Mockoon meliputi register, login session, Workspace, import, review, submission, deduplikasi, agenda, invitation, dan logout.

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

Test otomatis memakai database H2 terisolasi dan server HTTP fixture in-process; tidak mengubah PostgreSQL development. Jalankan smoke test setelah backend dan Mockoon hidup:

```powershell
.\scripts\smoke-test.ps1
```

## Docker

```powershell
$env:DB_PASSWORD = 'pilih-password-sendiri'
docker compose up --build
```

Compose menyediakan PostgreSQL, Mockoon, dan backend. Database menggunakan volume persisten, Mockoon terikat localhost port 3002, backend port 8081. Docker belum tersedia pada mesin development saat implementasi; Compose disediakan sebagai konfigurasi, bukan hasil deployment yang sudah diuji.

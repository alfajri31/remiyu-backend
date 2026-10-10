# Guide API Remiyu

Dokumen ini menjelaskan API backend Remiyu yang saat ini sudah dibuat: kegunaannya, request yang dikirim Flutter/client, dan response yang diterima. Acuan teknisnya adalah Controller dan DTO di `src/main/java`; jika ada perbedaan dengan dokumen lain, kode tersebut yang menjadi sumber kebenaran.

## Dasar API

| Item | Nilai |
|---|---|
| Base URL lokal | `http://localhost:8081` |
| Prefix API | `/api/v1` |
| Format request/response normal | JSON (`application/json`) |
| Upload chat / bukti tugas | `multipart/form-data` |
| Autentikasi | `Authorization: Bearer <accessToken>` |
| Token | Token acak, bukan JWT; masa aktif default 24 jam |

Endpoint publik hanya `POST /auth/register`, `POST /auth/login`, dan `GET /actuator/health`. Endpoint lain membutuhkan Bearer token.

Contoh header autentikasi:

```http
Authorization: Bearer eyJ...bukan-contoh-token-asli
```

## Bentuk error

Jika request gagal, backend umumnya mengembalikan bentuk berikut:

```json
{
  "timestamp": "2026-10-10T10:00:00Z",
  "status": 400,
  "message": "Data tidak valid.",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

| Status | Makna umum |
|---:|---|
| `400` | Format, parameter, atau validasi request tidak valid |
| `401` | Token tidak ada, salah, kadaluarsa, atau user belum login |
| `403` | User tidak memiliki akses ke Workspace/aksi tersebut |
| `404` | Data tidak ditemukan |
| `409` | Data sudah ada atau masih dipakai |
| `413` | File melebihi batas 1 MB |
| `500` | Kesalahan server atau gateway AI |

## Entity dan skema database

Dokumentasi tabel, kolom, relasi, constraint, dan diagram schema dipisahkan ke [ENTITY-GUIDE.md](entity-guide.md).

## 1. Kesehatan backend

### `GET /actuator/health`

Untuk memastikan backend hidup. Tidak membutuhkan token.

**Response `200`**

```json
{ "status": "UP" }
```

## Dashboard

### `GET /api/v1/dashboard`

Mengambil data ringkas untuk layar Dashboard user login. Semua angka hanya dihitung dari Workspace yang diikuti user. Dashboard memakai zona waktu `Asia/Jakarta` dan periode overview dimulai dari pukul `00:00` hari ini hingga sebelum pukul `00:00` 14 hari berikutnya.

- `taskCount`: tugas `TODO` atau `IN_PROGRESS` yang memiliki deadline pada periode overview.
- `agendaCount`: agenda `CONFIRMED` pada periode overview.
- `prioritiesToday`: tugas aktif yang deadline-nya hari ini, dikelompokkan berdasarkan prioritas.
- `nextTask` dan `nextAgenda`: aktivitas aktif/terkonfirmasi paling dekat setelah waktu request; bernilai `null` jika belum ada.

**Response `200`**

```json
{
  "period": {
    "startsAt": "2026-10-10T00:00:00+07:00",
    "endsAt": "2026-10-24T00:00:00+07:00"
  },
  "taskCount": 4,
  "agendaCount": 2,
  "prioritiesToday": { "high": 1, "medium": 2, "low": 0 },
  "nextTask": {
    "id": "uuid-task",
    "workspaceId": "uuid-workspace",
    "title": "Kirim proposal klien",
    "dueAt": "2026-10-10T17:00:00+07:00",
    "priority": "HIGH",
    "status": "IN_PROGRESS"
  },
  "nextAgenda": {
    "id": "uuid-agenda",
    "workspaceId": "uuid-workspace",
    "title": "Rapat koordinasi",
    "startsAt": "2026-10-11T09:00:00+07:00",
    "status": "CONFIRMED"
  }
}
```

## 2. Autentikasi

### `POST /api/v1/auth/register`

Membuat akun baru dan langsung mengembalikan sesi login.

**Request**

```json
{
  "name": "Dimas Pratama",
  "email": "dimas@example.com",
  "password": "minimal-8-karakter"
}
```

| Field | Wajib | Aturan |
|---|---|---|
| `name` | Ya | 1–100 karakter |
| `email` | Ya | Email valid, maksimal 254 karakter |
| `password` | Ya | 8–72 karakter |

**Response `201`**

```json
{
  "accessToken": "token-login",
  "tokenType": "Bearer",
  "expiresAt": "2026-10-11T10:00:00+07:00",
  "user": {
    "id": "uuid-user",
    "name": "Dimas Pratama",
    "email": "dimas@example.com"
  }
}
```

### `POST /api/v1/auth/login`

Masuk memakai email dan password.

**Request**

```json
{
  "email": "dimas@example.com",
  "password": "minimal-8-karakter"
}
```

**Response `200`** — sama seperti response register.

### `GET /api/v1/auth/me`

Mengambil identitas user dari token aktif.

**Response `200`**

```json
{
  "id": "uuid-user",
  "name": "Dimas Pratama",
  "email": "dimas@example.com"
}
```

### `POST /api/v1/auth/logout`

Mencabut Bearer token aktif.

**Request body:** tidak ada.  
**Response:** `204 No Content`.

## 3. Workspace dan anggota

Workspace adalah wadah data tugas, agenda, anggota, dan sumber chat. User pembuat menjadi admin.

### `GET /api/v1/workspaces`

Mengambil semua Workspace yang boleh diakses user login.

**Response `200`**

```json
[
  {
    "id": "uuid-workspace",
    "name": "Tim Operasional",
    "platform": "WHATSAPP",
    "account": "62812xxxxxxx",
    "connectionStatus": "NOT_CONNECTED",
    "billingStatus": "FREE",
    "memberCount": 1,
    "createdAt": "2026-10-10T10:00:00+07:00"
  }
]
```

### `POST /api/v1/workspaces`

Membuat Workspace. Saat ini channel baru berstatus `NOT_CONNECTED`; endpoint ini belum mengaktifkan integrasi realtime platform.

**Request**

```json
{
  "name": "Tim Operasional",
  "platform": "WHATSAPP",
  "account": "62812xxxxxxx"
}
```

| Field | Wajib | Nilai/aturan |
|---|---|---|
| `name` | Ya | Maks. 100 karakter |
| `platform` | Ya | `WHATSAPP`, `TELEGRAM`, atau `TEAMS` |
| `account` | Tidak | Maks. 254 karakter |

**Response `201`** — satu objek `WorkspaceView`, seperti item pada daftar Workspace.

Workspace pertama berstatus billing `FREE`; Workspace berikutnya dapat berstatus `PAYMENT_REQUIRED`. Pembayaran nyata belum diimplementasikan.

### `GET /api/v1/workspaces/{workspaceId}`

Mengambil detail satu Workspace. Response sama dengan `WorkspaceView` di atas.

### `GET /api/v1/workspaces/{workspaceId}/members`

Mengambil anggota Workspace.

**Response `200`**

```json
[
  {
    "id": "uuid-user",
    "name": "Dimas Pratama",
    "email": "dimas@example.com",
    "role": "ADMIN"
  }
]
```

### `POST /api/v1/workspaces/{workspaceId}/invitation`

Membuat token undangan keanggotaan Remiyu. Hanya admin Workspace yang dapat membuatnya.

**Response `200`**

```json
{
  "workspaceId": "uuid-workspace",
  "inviteToken": "uuid-token",
  "joinPath": "/api/v1/workspaces/join/uuid-token"
}
```

`joinPath` adalah endpoint API, bukan public/deep link dan bukan link grup WhatsApp/Telegram/Teams.

### `POST /api/v1/workspaces/join/{inviteToken}`

Menambahkan user login ke Workspace dari token undangan.

**Request body:** tidak ada.  
**Response `200`:** objek `WorkspaceView`.

## 4. Tugas

### `GET /api/v1/workspaces/{workspaceId}/tasks`

Mengambil tugas pada Workspace.

| Query | Wajib | Nilai |
|---|---|---|
| `status` | Tidak | `DRAFT`, `TODO`, `IN_PROGRESS`, `DONE` |
| `limit` | Tidak | 1–100; default `50` |
| `offset` | Tidak | Default `0` |

Contoh: `GET /tasks?status=TODO&limit=20&offset=0`

**Response `200`**

```json
[
  {
    "id": "uuid-task",
    "workspaceId": "uuid-workspace",
    "importId": null,
    "title": "Kirim proposal klien",
    "description": "Versi revisi untuk klien A.",
    "assigneeName": "Budi",
    "dueAt": "2026-10-12T17:00:00+07:00",
    "priority": "HIGH",
    "status": "TODO",
    "submitted": false,
    "sourceMessageIds": [],
    "createdAt": "2026-10-10T10:00:00+07:00",
    "updatedAt": "2026-10-10T10:00:00+07:00"
  }
]
```

### `GET /api/v1/workspaces/{workspaceId}/tasks/{taskId}`

Mengambil satu tugas. Response sama dengan satu item `TaskView` di atas.

### `POST /api/v1/workspaces/{workspaceId}/tasks`

Membuat tugas manual. Hanya admin Workspace.

**Request**

```json
{
  "title": "Kirim proposal klien",
  "description": "Versi revisi untuk klien A.",
  "assigneeName": "Budi",
  "dueAt": "2026-10-12T17:00:00+07:00",
  "priority": "HIGH"
}
```

| Field | Wajib | Aturan |
|---|---|---|
| `title` | Ya | Maks. 200 karakter |
| `description` | Tidak | Maks. 4.000 karakter |
| `assigneeName` | Tidak | Maks. 100 karakter |
| `dueAt` | Tidak | ISO-8601 offset datetime |
| `priority` | Ya | `LOW`, `MEDIUM`, atau `HIGH` |

**Response `201`:** objek `TaskView`.

### `PATCH /api/v1/workspaces/{workspaceId}/tasks/{taskId}`

Mengubah isi tugas. Request dan response sama seperti membuat tugas. Hanya admin.

### `PATCH /api/v1/workspaces/{workspaceId}/tasks/{taskId}/status`

Mengubah status tugas yang sudah dikonfirmasi. Anggota Workspace dapat memperbarui status.

**Request**

```json
{ "status": "IN_PROGRESS" }
```

Nilai status: `TODO`, `IN_PROGRESS`, atau `DONE`.

**Response `200`:** objek `TaskView` terbaru.

### `POST /api/v1/workspaces/{workspaceId}/tasks/{taskId}/confirm`

Mengonfirmasi draft hasil import/AI. Status tugas berubah dari `DRAFT` menjadi `TODO`. Hanya admin.

**Request body:** tidak ada.  
**Response `200`:** objek `TaskView` terbaru.

### `DELETE /api/v1/workspaces/{workspaceId}/tasks/{taskId}`

Menghapus tugas. Hanya admin.

**Response:** `204 No Content`.

## 5. Bukti/hasil tugas

### `POST /api/v1/workspaces/{workspaceId}/tasks/{taskId}/submissions`

Mengirim hasil tugas. Client dapat mengirim tautan, catatan, file, atau kombinasinya menggunakan `multipart/form-data`. File pilot disimpan di PostgreSQL dan maksimal 1 MB.

**Request form-data**

| Part | Wajib | Isi |
|---|---|---|
| `link` | Tidak | URL hasil tugas |
| `note` | Tidak | Catatan hasil tugas |
| `file` | Tidak | File bukti, maks. 1 MB |

Contoh cURL:

```bash
curl -X POST "http://localhost:8081/api/v1/workspaces/{workspaceId}/tasks/{taskId}/submissions" \
  -H "Authorization: Bearer {token}" \
  -F "link=https://example.com/hasil" \
  -F "note=Hasil sudah selesai" \
  -F "file=@hasil.pdf"
```

**Response `201`**

```json
{
  "id": "uuid-submission",
  "taskId": "uuid-task",
  "userId": "uuid-user",
  "link": "https://example.com/hasil",
  "note": "Hasil sudah selesai",
  "filename": "hasil.pdf",
  "createdAt": "2026-10-10T10:00:00+07:00"
}
```

Pengiriman berhasil akan membuat status tugas menjadi `DONE` dalam transaksi yang sama.

### `GET /api/v1/workspaces/{workspaceId}/tasks/{taskId}/submissions`

Mengambil metadata semua submission suatu tugas.

**Response `200`:** array `SubmissionView`. Endpoint download isi file belum tersedia.

## 6. Import chat dan hasil AI

### `POST /api/v1/workspaces/{workspaceId}/imports`

Mengunggah export chat WhatsApp `.txt` atau `.zip` yang berisi tepat satu file chat `.txt`. Backend mem-parsing pesan baru, memanggil gateway AI (saat development: Mockoon), lalu menyimpan tugas dan agenda sebagai draft.

**Request form-data**

| Part | Wajib | Aturan |
|---|---|---|
| `file` | Ya | `.txt` atau `.zip`, maks. 1 MB |

**Response `201`**

```json
{
  "importId": "uuid-import",
  "newMessages": 20,
  "duplicateMessages": 5,
  "taskIds": ["uuid-task-draft"],
  "agendaIds": ["uuid-agenda-draft"],
  "status": "COMPLETED",
  "aiProvider": "mockoon"
}
```

Import ulang pesan yang sama tidak membuat draft duplikat; bila seluruh pesan sudah pernah masuk, AI tidak dipanggil ulang.

### `GET /api/v1/workspaces/{workspaceId}/imports`

Mengambil riwayat import Workspace.

**Response `200`**

```json
[
  {
    "id": "uuid-import",
    "workspaceId": "uuid-workspace",
    "filename": "chat-tim.txt",
    "newMessages": 20,
    "duplicateMessages": 5,
    "aiProvider": "mockoon",
    "status": "COMPLETED",
    "createdAt": "2026-10-10T10:00:00+07:00"
  }
]
```

## 7. Pesan sumber

### `GET /api/v1/workspaces/{workspaceId}/messages?ids={uuid}&ids={uuid}`

Mengambil pesan chat sumber untuk menjelaskan asal suatu tugas/agenda. Parameter `ids` wajib dikirim 1–100 kali.

**Response `200`**

```json
[
  {
    "id": "uuid-message",
    "sender": "Budi",
    "sentAt": "2026-10-10T09:30:00+07:00",
    "text": "Tolong kirim proposal sebelum sore."
  }
]
```

## 8. Agenda

### `GET /api/v1/workspaces/{workspaceId}/agendas`

Mengambil agenda pada Workspace.

| Query | Wajib | Nilai |
|---|---|---|
| `limit` | Tidak | 1–100; default `50` |
| `offset` | Tidak | Default `0` |

**Response `200`**

```json
[
  {
    "id": "uuid-agenda",
    "title": "Daily Team Meeting",
    "startsAt": "2026-10-12T09:00:00+07:00",
    "status": "DRAFT",
    "sourceMessageIds": ["uuid-message"]
  }
]
```

### `PATCH /api/v1/workspaces/{workspaceId}/agendas/{agendaId}`

Mengubah agenda draft. Hanya admin.

**Request**

```json
{
  "title": "Daily Team Meeting",
  "startsAt": "2026-10-12T09:00:00+07:00"
}
```

| Field | Wajib | Aturan |
|---|---|---|
| `title` | Ya | Maks. 200 karakter |
| `startsAt` | Tidak | ISO-8601 offset datetime |

**Response `200`:** objek `AgendaView` terbaru.

### `POST /api/v1/workspaces/{workspaceId}/agendas/{agendaId}/confirm`

Mengonfirmasi agenda draft. Status berubah dari `DRAFT` menjadi `CONFIRMED`. Hanya admin.

**Response `200`:** objek `AgendaView` terbaru.

### `DELETE /api/v1/workspaces/{workspaceId}/agendas/{agendaId}`

Menghapus agenda. Hanya admin.

**Response:** `204 No Content`.

## Alur Flutter yang disarankan

```text
Register/Login
→ simpan accessToken di secure storage
→ GET /auth/me untuk memulihkan sesi
→ GET /workspaces
→ pilih atau buat Workspace
→ import chat / kelola tugas / agenda
→ saat logout: POST /auth/logout lalu hapus token lokal
```

Untuk import:

```text
Pilih Workspace
→ POST /imports dengan file chat
→ GET /tasks?status=DRAFT dan GET /agendas
→ admin edit bila perlu
→ POST /confirm untuk tugas dan agenda
→ anggota mengubah status tugas atau mengirim submission
```

## Batas implementasi saat ini

- AI masih fixture Mockoon pada development; belum inferensi OpenAI production.
- Channel realtime, webhook WhatsApp/Telegram/Teams, payment gateway, push notification, email reset/verification, dan public/deep invitation link belum dibuat.
- Endpoint submission belum menyediakan download file, object storage, antivirus, atau idempotency key.
- Semua ID pada path (`workspaceId`, `taskId`, `agendaId`) adalah UUID.

## Dokumen pendukung

- [README.md](README.md): cara menjalankan backend, arsitektur, seeder, dan Docker.
- [docs/requests.http](docs/requests.http): request siap pakai untuk JetBrains HTTP Client.
- [docs/AI-CONTRACT.md](docs/AI-CONTRACT.md): kontrak Spring Boot ke Mockoon/FastAPI.

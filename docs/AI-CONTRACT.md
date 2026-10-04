# Kontrak AI gateway Remiyu

Spring Boot memanggil `POST {AI_BASE_URL}{AI_EXTRACT_PATH}`. Default: http://localhost:3002/v1/extractions di Mockoon. Ini kontrak service AI Remiyu yang nanti diimplementasikan FastAPI; bukan endpoint OpenAI langsung.

Request JSON:

```json
{
  "requestId": "69b66fc9-929c-4bda-a293-186366d4567c",
  "workspaceId": "414e7e48-7f65-4b2f-9d50-c5996e6645c3",
  "messages": [
    {
      "id": "a31cff7e-77cb-45fd-b2ec-0f723df444a4",
      "sender": "Budi",
      "sentAt": "2026-10-04T09:00:00+07:00",
      "text": "Tolong kirim proposal klien."
    }
  ]
}
```

Response HTTP 200:

```json
{
  "tasks": [
    {
      "title": "Kirim proposal klien",
      "description": null,
      "assigneeName": null,
      "dueAt": null,
      "priority": "MEDIUM",
      "sourceMessageIds": ["a31cff7e-77cb-45fd-b2ec-0f723df444a4"]
    }
  ],
  "agendas": []
}
```

Kontrak:
- `tasks` dan `agendas` wajib berupa array, boleh kosong; maksimal 100 item per jenis.
- Setiap item wajib punya title nonblank, maksimal 200 karakter, dan 1–100 source-message IDs yang ada pada request ini.
- Priority tugas: LOW, MEDIUM, HIGH. Assignee/deadline yang tidak diketahui harus null.
- Agenda: title, nullable startsAt (ISO-8601 dengan offset), sourceMessageIds.
- Spring Boot menetapkan status DRAFT; provider AI tidak boleh langsung menerbitkan tugas.
- Kesalahan HTTP, JSON invalid, atau source IDs asing menghasilkan 502 dan rollback import.
- Mockoon selalu menghasilkan contoh satu tugas dan satu agenda dari pesan pertama. Ini simulasi alur review, bukan ekstraksi yang sebenarnya.
- Endpoint `/v1/extractions-failure` di Mockoon menghasilkan 503 untuk pengujian; pilih lewat AI_EXTRACT_PATH sebelum restart backend.

Mengganti service ke FastAPI:

```powershell
$env:AI_BASE_URL = 'http://localhost:8000'
$env:AI_EXTRACT_PATH = '/v1/extractions'
$env:AI_PROVIDER = 'fastapi'
```

FastAPI dapat menggunakan adapter OpenAI atau local LLM dengan struktur response identik. AI_API_KEY opsional mengirim Bearer token antarservice. API key OpenAI nantinya hanya disimpan di service AI.


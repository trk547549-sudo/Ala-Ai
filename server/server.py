import os
import json
import urllib.request
import urllib.error
import traceback
from http.server import BaseHTTPRequestHandler, HTTPServer

MODEL = os.environ.get("GEMINI_MODEL", "gemini-3.5-flash-lite")

class Handler(BaseHTTPRequestHandler):
    def send_json(self, status, data):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/chat":
            return self.send_json(404, {"error": "المسار غير موجود"})

        key = os.environ.get("GEMINI_API_KEY")
        if not key:
            return self.send_json(503, {"error": "مفتاح الخدمة غير مضبوط على الخادم"})

        try:
            size = int(self.headers.get("Content-Length", 0))
            if size < 1 or size > 8192:
                return self.send_json(400, {"error": "حجم الرسالة غير صالح"})

            data = json.loads(self.rfile.read(size))
            message = str(data.get("message", "")).strip()

            if not message:
                return self.send_json(400, {"error": "اكتب رسالة أولاً"})

            payload = {
                "contents": [{
                    "parts": [{"text": message}]
                }]
            }

            url = (
                "https://generativelanguage.googleapis.com/v1beta/"
                f"models/{MODEL}:generateContent"
            )

            request = urllib.request.Request(
                url,
                data=json.dumps(payload).encode("utf-8"),
                headers={
                    "Content-Type": "application/json",
                    "x-goog-api-key": key
                },
                method="POST"
            )

            with urllib.request.urlopen(request, timeout=60) as response:
                result = json.loads(response.read().decode("utf-8"))

            answer = "".join(
                part.get("text", "")
                for part in result["candidates"][0]["content"]["parts"]
                if "text" in part
            )

            self.send_json(200, {"reply": answer or "لم يصل رد نصي من النموذج"})

        except urllib.error.HTTPError as e:
            self.send_json(502, {"error": f"رفضت خدمة Gemini الطلب (HTTP {e.code})"})
        except Exception:
            traceback.print_exc()
            self.send_json(500, {"error": "حدث خطأ أثناء معالجة الرسالة"})

    def log_message(self, format, *args):
        pass

print("ALA AI server running at http://127.0.0.1:5050")
HTTPServer(("127.0.0.1", 5050), Handler).serve_forever()

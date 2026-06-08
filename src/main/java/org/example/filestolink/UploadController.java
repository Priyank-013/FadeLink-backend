package org.example.filestolink;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@CrossOrigin(origins = "*")
@RestController
public class UploadController {

    HashMap<String, FileData> map = new HashMap<>();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile[] files,
            @RequestParam(value = "zipName", required = false) String zipName
    ) {
        try {
            if (zipName == null || zipName.isBlank()) {
                zipName = "files";
            }

            if (files.length == 0) {
                return ResponseEntity.badRequest().body("No files selected");
            }

            List<byte[]> fileBytesList = new ArrayList<>();
            List<String> originalNames = new ArrayList<>();

            for (MultipartFile f : files) {
                if (f.isEmpty()) continue;
                fileBytesList.add(f.getBytes());
                originalNames.add(f.getOriginalFilename());
            }

            if (fileBytesList.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid files uploaded");
            }

            String token = UUID.randomUUID().toString();
            String BASE_URL = "https://fadelink-backend-production.up.railway.app";

            String viewLink = BASE_URL + "/file/" + token;
            String downloadLink = BASE_URL + "/download/" + token;
            ZonedDateTime expTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusHours(3);

            FileData fileData = new FileData(fileBytesList, originalNames, expTime, zipName);
            map.put(token, fileData);

            HashMap<String, Object> response = new HashMap<>();
            response.put("message", originalNames.size() + " files uploaded successfully");
            response.put("files", originalNames);
            response.put("viewLink", viewLink);
            response.put("downloadZip", downloadLink);
            response.put("expiresAt", expTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/file/{token}")
    public ResponseEntity<?> getFile(@PathVariable String token) {

        if (!map.containsKey(token)) {
            return ResponseEntity.badRequest().body("Invalid Token");
        }

        FileData data = map.get(token);

        if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(data.getExpTime())) {
            map.remove(token);
            return ResponseEntity.badRequest().body("Link expired");
        }

        StringBuilder html = new StringBuilder();
        html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Shared Files</title>
            <style>
                @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600&display=swap');
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                    font-family: 'DM Sans', sans-serif;
                    min-height: 100vh;
                    background: #0f0e17;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 2rem 1rem;
                }
                .blob {
                    position: fixed;
                    border-radius: 50%;
                    filter: blur(80px);
                    opacity: 0.18;
                    pointer-events: none;
                    z-index: 0;
                }
                .blob-1 { width: 600px; height: 600px; background: #6c63ff; top: -150px; left: -150px; }
                .blob-2 { width: 500px; height: 500px; background: #00d2a0; bottom: -100px; right: -100px; }
                .blob-3 { width: 350px; height: 350px; background: #ff6b8a; top: 40%; left: 55%; }
                .card {
                    position: relative;
                    z-index: 1;
                    width: 100%;
                    max-width: 460px;
                    background: rgba(255,255,255,0.055);
                    border: 1px solid rgba(255,255,255,0.10);
                    border-radius: 24px;
                    padding: 2.5rem 2.25rem;
                    backdrop-filter: blur(28px);
                    color: rgba(255,255,255,0.85);
                }
                h1 { font-size: 22px; font-weight: 600; color: #f4f0ff; margin-bottom: 0.4rem; text-align: center; }
                .subtitle { font-size: 13px; color: rgba(255,255,255,0.35); text-align: center; margin-bottom: 2rem; }
                .file-row {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    gap: 10px;
                    background: rgba(255,255,255,0.05);
                    border: 1px solid rgba(255,255,255,0.09);
                    border-radius: 11px;
                    padding: 11px 14px;
                    margin-bottom: 10px;
                }
                .file-name { font-size: 14px; color: rgba(255,255,255,0.75); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .dl-btn {
                    background: linear-gradient(135deg, #6c63ff, #00d2a0);
                    color: #fff;
                    border: none;
                    border-radius: 8px;
                    padding: 7px 14px;
                    font-size: 13px;
                    font-weight: 600;
                    font-family: 'DM Sans', sans-serif;
                    cursor: pointer;
                    text-decoration: none;
                    white-space: nowrap;
                    transition: opacity 0.2s;
                    flex-shrink: 0;
                }
                .dl-btn:hover { opacity: 0.85; }
            </style>
        </head>
        <body>
            <div class="blob blob-1"></div>
            <div class="blob blob-2"></div>
            <div class="blob blob-3"></div>
            <div class="card">
                <h1>Shared Files</h1>
                <p class="subtitle">Click to download individual files</p>
        """);

        List<String> names = data.getOriginalNames();
        for (int i = 0; i < names.size(); i++) {
            html.append("<div class='file-row'>")
                    .append("<span class='file-name'>").append(names.get(i)).append("</span>")
                    .append("<a class='dl-btn' href='/download/").append(token).append("/").append(i).append("'>↓ Download</a>")
                    .append("</div>");
        }

        html.append("""
                </div>
            </body>
        </html>
        """);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html.toString());
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<?> downloadZip(@PathVariable String token) {

        if (!map.containsKey(token)) {
            return ResponseEntity.badRequest().body("Invalid Token");
        }

        FileData data = map.get(token);

            if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(data.getExpTime())) {
            map.remove(token);
            return ResponseEntity.badRequest().body("Link expired");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (int i = 0; i < data.getFileBytes().size(); i++) {
                zos.putNextEntry(new ZipEntry(data.getOriginalNames().get(i)));
                zos.write(data.getFileBytes().get(i));
                zos.closeEntry();
            }

            zos.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + data.getZipName() + ".zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{token}/{index}")
    public ResponseEntity<?> downloadSingleFile(
            @PathVariable String token,
            @PathVariable int index
    ) {
        if (!map.containsKey(token)) {
            return ResponseEntity.badRequest().body("Invalid Token");
        }

        FileData data = map.get(token);

            if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(data.getExpTime())) {
            map.remove(token);
            return ResponseEntity.badRequest().body("Link expired");
        }

        if (index < 0 || index >= data.getFileBytes().size()) {
            return ResponseEntity.badRequest().body("Invalid file index");
        }

        try {
            byte[] fileBytes = data.getFileBytes().get(index);
            String originalName = data.getOriginalNames().get(index);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
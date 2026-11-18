package com.example.demo.model.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileStoreTests {

    @TempDir
    Path uploadDir;

    @Test
    void storesPngWithServerGeneratedName() throws Exception {
        FileStore store = new FileStore(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "poster",
                "../../poster.png",
                "image/png",
                new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0});

        String storedName = store.save(file);

        assertNotNull(storedName);
        assertTrue(storedName.matches("[0-9a-f-]+\\.png"));
        assertTrue(Files.exists(uploadDir.resolve(storedName)));
    }

    @Test
    void rejectsFakePng() throws Exception {
        FileStore store = new FileStore(uploadDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "poster", "fake.png", "image/png", "not an image".getBytes());

        assertThrows(IllegalArgumentException.class, () -> store.save(file));
    }
}

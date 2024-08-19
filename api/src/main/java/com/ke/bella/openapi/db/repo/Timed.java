package com.ke.bella.openapi.db.repo;

import java.time.LocalDateTime;

public interface Timed {
    LocalDateTime getCtime();

    void setCtime(LocalDateTime ctime);

    LocalDateTime getMtime();

    void setMtime(LocalDateTime mtime);
}

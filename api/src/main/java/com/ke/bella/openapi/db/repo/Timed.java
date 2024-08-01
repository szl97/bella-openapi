package com.ke.bella.openapi.db.repo;

import java.sql.Timestamp;

public interface Timed {
    Timestamp getCtime();

    void setCtime(Timestamp ctime);

    Timestamp getMtime();

    void setMtime(Timestamp mtime);
}

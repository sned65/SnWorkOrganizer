package sne.workorganizer.db;

/**
 * A helper interface which defines constants for work with the DB.
 */
/* package private */ interface DbSchema
{
    String DATABASE_NAME = "SnWorkOrganizer.db";
    int SCHEMA_VERSION = 1;

    String TBL_CLIENTS = "clients";
    String TBL_PROJECTS = "projects";
    String TBL_PICTURES = "pictures";

    // CLIENTS TABLE

    String CLIENTS_PK = "client_id";
    String CLIENTS_COL_FULLNAME = "fullname";
    String CLIENTS_COL_PHONE = "phone";
    String CLIENTS_COL_EMAIL = "email";
    String CLIENTS_COL_SOCIAL = "social";
}

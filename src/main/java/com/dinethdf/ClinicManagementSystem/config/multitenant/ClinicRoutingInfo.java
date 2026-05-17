package com.dinethdf.ClinicManagementSystem.config.multitenant;

/**
 * Lightweight routing record for a single clinic.
 * Loaded from clinicmsystem_master.clinics at startup into ClinicRoutingRegistry.
 *
 * Contains ONLY what is needed to route a request to the correct datasource.
 * No settings, no secrets, no full clinic data — keep it minimal.
 */
public class ClinicRoutingInfo {

    private Integer clinicId;       // e.g. 1
    private String  clinicCode;     // e.g. "abc-123-uuid"  (UUID from clinics table)
    private String  subdomain;      // e.g. "hospital1"     (for subdomain-based resolution)
    private String  schemaName;     // e.g. "clinic_abc123uuid"  OR  "shared"
    private boolean schemaMode;     // true = SCHEMA (own DB), false = SHARED (clinicmsystem)
    private String  planCode;       // e.g. "PLAN_STANDARD"

    // ── Constructor ──────────────────────────────────────────────────────────────

    public ClinicRoutingInfo(Integer clinicId, String clinicCode,
                             String subdomain, String schemaName, boolean schemaMode, String planCode) {
        this.clinicId   = clinicId;
        this.clinicCode = clinicCode;
        this.subdomain  = subdomain;
        this.schemaName = schemaName;
        this.schemaMode = schemaMode;
        this.planCode   = planCode;
    }

    // ── Static Factory Methods ───────────────────────────────────────────────────

    /**
     * Creates routing info for a SCHEMA mode clinic (dedicated database).
     * schemaName is auto-derived: "clinic_" + clinicCode with hyphens removed.
     */
    public static ClinicRoutingInfo forSchema(Integer clinicId, String clinicCode, String subdomain, String planCode) {
        String schemaName = "clinic_" + clinicCode.replace("-", "");
        return new ClinicRoutingInfo(clinicId, clinicCode, subdomain, schemaName, true, planCode);
    }

    /**
     * Creates routing info for a SHARED mode clinic (clinicmsystem DB, table-level isolation).
     */
    public static ClinicRoutingInfo forShared(Integer clinicId, String clinicCode, String subdomain, String planCode) {
        return new ClinicRoutingInfo(clinicId, clinicCode, subdomain, "shared", false, planCode);
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public Integer getClinicId()   { return clinicId;   }
    public String  getClinicCode() { return clinicCode; }
    public String  getSubdomain()  { return subdomain;  }
    public String  getSchemaName() { return schemaName; }
    public boolean isSchemaMode()  { return schemaMode; }
    public String  getPlanCode()   { return planCode;   }

    @Override
    public String toString() {
        return "ClinicRoutingInfo{id=" + clinicId
                + ", code='" + clinicCode + "'"
                + ", schema='" + schemaName + "'"
                + ", mode=" + (schemaMode ? "SCHEMA" : "SHARED") + "}";
    }
}

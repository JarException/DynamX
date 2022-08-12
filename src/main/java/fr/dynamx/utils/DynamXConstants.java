package fr.dynamx.utils;

public class DynamXConstants
{
    public static final String NAME = "DynamX";
    public static final String ID = "dynamxmod";
    public static final String VERSION = "3.3.1";
    public static final String VERSION_TYPE = "Beta";
    public static final String RES_DIR_NAME = "DynamX";

    public static final String ACS_GUIS_BASE_URL = "https://dynamx.fr/download/libs/ACsGuis-";
    public static final String DEFAULT_ACSGUIS_VERSION = "1.2.1-3-all";

    public static final String LIBBULLET_VERSION = "14.3.0";
    /** .dc file version, only change when an update of libbullet breaks the .dc files, to regenerate them */
    public static final String DC_FILE_VERSION = "12.5.0";

    public static final String DYNAMX_CERT = "certs/lets-encrypt-r3.der", DYNAMX_AUX_CERT = null;
    public static final String MPS_URL = "https://dynamx.fr/mps/", MPS_AUX_URL = null, MPS_KEY = "0", MPS_STARTER = null;

    public static final String STATS_URL = "https://dynamx.fr/statsbot/statsbotrcv.php", STATS_PRODUCT = "DNX_"+VERSION+"_BETA", STATS_TOKEN = "0";
}
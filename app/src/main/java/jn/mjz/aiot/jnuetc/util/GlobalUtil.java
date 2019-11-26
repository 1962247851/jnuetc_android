package jn.mjz.aiot.jnuetc.util;

import jn.mjz.aiot.jnuetc.R;

/**
 * @author 19622
 */
public class GlobalUtil {
    private static GlobalUtil instance;
//            private static final String HOST = "https://www.jiangnan-dzjsb.club/RepairDev/";
    private static final String HOST = "https://www.jiangnan-dzjsb.club/Repair/";

    public static final String STATE_OK = "OK";

    public static final String[] TITLES_N = {"杏园", "桃园", "桔园", "桂园", "梅园", "榴园", "李园", "竹园"};
    public static final String[] TITLES_S = {"澈苑", "溪苑", "清苑", "涓苑", "润苑", "浩苑", "瀚苑", "鸿苑"};
    public static final String[] MARKS = {"小白", "半小白", "略懂", "精通"};
    public static final String[] SERVICES = {"系统问题", "网络故障", "路由器安装", "硬件故障", "拆机清灰", "硬件安装（笔记本安装内存或固态）", "台式机组装", "软件安装", "其它"};
    public static final String[] LOCATIONS = {"杏园", "桃园", "桔园", "桂园", "梅园", "榴园", "李园", "竹园", "澈苑", "溪苑", "清苑", "涓苑", "润苑", "浩苑", "瀚苑", "鸿苑"};
    public static final String[] COLLEGES = {"物联网工程学院", "设计学院", "商学院", "机械学院", "环境与土木学院", "食品学院", "纺织服装学院", "药学院", "外国语学院", "数字媒体学院", "生物工程学院", "化学与材料工程学院", "理学院", "医学院", "人文学院", "法学院", "马克思主义学院", "北美学院"};
    public static final String[] GRADES = {"大一", "大二", "大三", "大四", "研究生", "其他"};

    public static final int[] UN_SELECTED_ICON_IDS = {R.drawable.ic_new_task_unselect, R.drawable.ic_second_unselect, R.drawable.ic_myself_unselect,};
    public static final int[] SELECTED_ICON_IDS = {R.drawable.ic_new_task_selected, R.drawable.ic_second_selected, R.drawable.ic_myself_selected,};

    public static final class KEYS {

        public static final class LOGIN_ACTIVITY {
            public static final String FILE_NAME = "login_info";
            public static final String REMEMBER_PASSWORD = "0";
            public static final String AUTO_LOGIN = "1";
            public static final String USER_JSON_STRING = "2";
        }

        public static final class NEW {
            public static final String FILE_NAME_DRAWER = "drawerSettings1";
        }

        public static final class PROCESSING {
            public static final String FILE_NAME_DRAWER = "drawerSettings2";
        }

        public static final class DONE {
            public static final String FILE_NAME_DRAWER = "drawerSettings3";
        }

    }


    public static final class URLS {

        public static final class FILE {
            public static final String DOWNLOAD = HOST + "Download";
            public static final String UPLOAD_TIP_DP = HOST + "UploadTipDp";
        }


        public static final class UPDATE {
            public static final String FEEDBACK = HOST + "Feedback";
            public static final String MODIFY_PASSWORD = HOST + "UpdatePassword";
            public static final String MODIFY = HOST + "Modify";
            public static final String START_SERVICE = HOST + "StartService";
            public static final String START_DAY_DP_SERVICE = HOST + "StartDayDPService";
            public static final String CLOSE_SERVICE = HOST + "CloseService";
            public static final String CLOSE_DAY_DP_SERVICE = HOST + "CloseDayDPService";

        }

        public static final class QUERY {
            public static final String BY_ID = HOST + "QueryById";
            public static final String ALL = HOST + "QueryAll";
            public static final String ALL_USER = HOST + "QueryAllUser";
            public static final String LOGIN = HOST + "Login";
            public static final String CHECK_STATE = HOST + "CheckState";
            public static final String HAVE_ROOT = HOST + "HaveRoot";
            public static final String CHECK_FOR_UPDATE = HOST + "CheckUpdate";
            public static final String CHECK_HISTORY = HOST + "CheckHistory";
            public static final String SELECT_ALL_TIP_DP = HOST + "selectAllTipDp";

        }

        public static final class INSERT {
            public static final String INSERT = HOST + "Insert";
            public static final String INSERT_CODE = HOST + "InsertNewCode";
            public static final String INSERT_TIP_DP = HOST + "insertTipDp";
        }

        public static final class DELETE {
            public static final String DELETE = HOST + "Delete";
            public static final String DELETE_MANY = HOST + "DeleteMany";
        }

    }

    public static final class TITLES {
        public static final class ACTIVITY {
            public static final String[] MAIN_ACTIVITY = {"未处理", "已处理", "我的"};

        }
    }

    public static GlobalUtil getInstance() {
        if (instance == null) {
            instance = new GlobalUtil();
        }
        return instance;
    }

}


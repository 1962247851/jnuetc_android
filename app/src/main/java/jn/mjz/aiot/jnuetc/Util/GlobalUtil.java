package jn.mjz.aiot.jnuetc.Util;

import jn.mjz.aiot.jnuetc.Greendao.Entity.User;
import jn.mjz.aiot.jnuetc.R;

public class GlobalUtil {
    private static GlobalUtil instance;
    private static final String HOST = "http://www.mjz-aiot.top:8080/Repair/";
    public static User user;

    public static final String[] titlesN = {"杏园", "桃园", "桔园", "桂园", "梅园", "榴园", "李园", "竹园"};
    public static final String[] titlesS = {"澈苑", "溪苑", "清苑", "涓苑", "润苑", "浩苑", "瀚苑", "鸿苑"};
    public static final String[] MARKS = {"小白", "半小白", "略懂", "精通"};
    public static final String[] SERVICES = {"系统问题", "网络故障", "路由器安装", "硬件故障", "拆机清灰", "硬件安装（笔记本安装内存或固态）", "台式机组装", "软件安装", "其它"};

    public static final int[] unSelectedIconIds = {R.drawable.ic_new_task_unselect, R.drawable.ic_second_unselect, R.drawable.ic_myself_unselect,};
    public static final int[] selectedIconIds = {R.drawable.ic_new_task_selected, R.drawable.ic_second_selected, R.drawable.ic_myself_selected,};

    public static final class KEYS {

        public static final class LOGIN_ACTIVITY {
            public static final String FILE_NAME = "login_info";
            public static final String REMEMBER_PASSWORD = "0";
            public static final String AUTO_LOGIN = "1";
            public static final String USER_NUMBER = "2";
            public static final String USER_PASSWORD = "3";
            public static final String USER_JSON_STRING = "4";
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
        }


        public static final class UPDATE {
            public static final String FEEDBACK = HOST + "Feedback";
            public static final String MODIFY_PASSWORD = HOST + "UpdatePassword";
            public static final String MODIFY = HOST + "Modify";
            public static final String START_SERVICE = HOST + "StartService";
            public static final String CLOSE_SERVICE = HOST + "CloseService";

        }

        public static final class QUERY {
            public static final String BY_ID = HOST + "QueryById";
            public static final String ALL = HOST + "QueryAll";
            public static final String LOGIN = HOST + "Login";
            public static final String CHECK_STATE = HOST + "CheckState";
            public static final String HAVE_ROOT = HOST + "HaveRoot";
            public static final String CHECK_FOR_UPDATE = HOST + "CheckUpdate";

        }

        public static final class INSERT {
            public static final String INSERT = HOST + "Insert";
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
        if (instance == null)
            instance = new GlobalUtil();
        return instance;
    }

}


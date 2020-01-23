package jn.mjz.aiot.jnuetc.util;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.youth.xframe.utils.http.HttpCallBack;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import jn.mjz.aiot.jnuetc.application.App;
import jn.mjz.aiot.jnuetc.greendao.Dao.DataDao;
import jn.mjz.aiot.jnuetc.greendao.entity.Data;

/**
 * @author 19622
 */
public class FileUtil {
    public static final int WRITE_REQUEST_CODE = 43;
    public static final int READ_REQUEST_CODE = 44;
    public static final int PIC_PICTURE = 45;
//    private static final String TAG = "FileUtil";

    public static void exportDatasToExcel(AppCompatActivity activity, Uri uri, IOnExportListener onExportListener) {
        // 设置第一行名
        String[] title = {"报修序号", "报修时间", "状态", "报修人", "学院", "年级", "电话", "QQ", "园区", "南北区", "设备型号", "问题详情", "维修人", "接单时间", "维修时间", "对用户电脑水平评估", "服务内容", "故障描述及解决过程"};
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet();

        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = null;

        // 写入第一行
        for (int i = 0; i < title.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(title[i]);
        }
        DataDao dataDao = App.getDaoSession().getDataDao();
        List<Data> dataList = dataDao.loadAll();

        //写入数据
        for (int i = 1; i <= dataList.size(); i++) {

            HSSFRow nextrow = sheet.createRow(i);
            Data data = dataList.get(i - 1);
            HSSFCell cell2 = nextrow.createCell(0);
            cell2.setCellValue(data.getId());

            cell2 = nextrow.createCell(1);
            cell2.setCellValue(DateUtil.getDateAndTime(data.getDate(), " "));

            cell2 = nextrow.createCell(2);
            cell2.setCellValue(data.getState() == 0 ? "未处理" : data.getState() == 1 ? "已接单" : "已维修");

            cell2 = nextrow.createCell(3);
            cell2.setCellValue(data.getName());

            cell2 = nextrow.createCell(4);
            cell2.setCellValue(data.getCollege());

            cell2 = nextrow.createCell(5);
            cell2.setCellValue(data.getGrade());

            cell2 = nextrow.createCell(6);
            cell2.setCellValue(data.getTel());

            cell2 = nextrow.createCell(7);
            cell2.setCellValue(data.getQq());

            cell2 = nextrow.createCell(8);
            cell2.setCellValue(data.getLocal());

            cell2 = nextrow.createCell(9);
            cell2.setCellValue(data.getDistrict() == 0 ? "北区" : "南区");

            cell2 = nextrow.createCell(10);
            cell2.setCellValue(data.getModel());

            cell2 = nextrow.createCell(11);
            cell2.setCellValue(data.getMessage());

            cell2 = nextrow.createCell(12);
            cell2.setCellValue(data.getRepairer());

            cell2 = nextrow.createCell(13);
            cell2.setCellValue(data.getState() != 0 ? DateUtil.getDateAndTime(data.getOrderDate(), " ") : "");

            cell2 = nextrow.createCell(14);
            cell2.setCellValue(data.getState() == 2 ? DateUtil.getDateAndTime(data.getRepairDate(), " ") : "");

            cell2 = nextrow.createCell(15);
            cell2.setCellValue(data.getMark());

            cell2 = nextrow.createCell(16);
            cell2.setCellValue(data.getService());

            cell2 = nextrow.createCell(17);
            cell2.setCellValue(data.getRepairMessage());

        }

        try {
            FileOutputStream fos = getFosFromUri(activity.getContentResolver(), uri, "rw");
            workbook.write(fos);
            fos.close();
            onExportListener.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            onExportListener.onError();
        }
    }

    public static void uploadTipDp(String fileName, File file, HttpCallBack<Boolean> callBack) {
        if (file != null && file.length() != 0) {
            HttpUtil.Post.uploadFile(GlobalUtil.Urls.File.UPLOAD, fileName, "/opt/dayDP/", file, new HttpUtil.HttpUtilCallBack<String>() {
                @Override
                public void onResponse(String result) {
//                    Log.e(TAG, "uploadTipDp onResponse: " + result);
                    JsonObject jsonObject = GsonUtil.getInstance().fromJson(result, JsonObject.class);
                    int error = jsonObject.get("error").getAsInt();
                    if (error == 1) {
                        callBack.onSuccess(true);
                    } else {
                        callBack.onSuccess(false);
                    }
                }

                @Override
                public void onFailure(String error) {
                    callBack.onFailed(error);
//                    Log.e(TAG, "uploadTipDp onFailure: " + e.getMessage());
                }
            });
        } else {
//            Log.e(TAG, "uploadTipDp: 文件不存在");
        }
    }

    public static void createFile(AppCompatActivity activity, String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public static void selectFile(AppCompatActivity activity, String mimeType) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(mimeType);

        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * @param contentResolver contentResolver
     * @param uri             uri The URI whose file is to be opened.
     * @param mode            mode Access mode for the file.  May be "r" for read-only access,
     *                        "rw" for read and write access, or "rwt" for read and write access
     *                        that truncates any existing file.
     * @return fileOutputStream
     */
    public static FileOutputStream getFosFromUri(ContentResolver contentResolver, Uri uri, @NotNull String mode) {
        ParcelFileDescriptor pfd = null;
        FileOutputStream fileOutputStream = null;
        try {
            pfd = contentResolver.
                    openFileDescriptor(uri, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pfd != null && pfd.getFileDescriptor() != null) {
            fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
        }
        return fileOutputStream;
    }

    /**
     * 跳转相册
     *
     * @param activity activity
     */
    public static void toPicture(AppCompatActivity activity) {
        //跳转到 ACTION_IMAGE_CAPTURE
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, PIC_PICTURE);
    }

    public static File uriToFile(Uri uri, Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }

    public interface IOnExportListener {
        void onSuccess();

        void onError();
    }
}

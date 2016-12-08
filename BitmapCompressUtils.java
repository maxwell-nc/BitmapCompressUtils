import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * 图片压缩工具类
 */
public class BitmapCompressUtils {

    /**
     * 缩小Bitmap
     *
     * @param bitmap    原图
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return 缩小后的图片, 无需缩放返回null
     */
    public static Bitmap scale(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();
        float sx = (float) reqWidth / (float) bitmap.getWidth();
        float sy = (float) reqHeight / (float) bitmap.getHeight();

        //无需缩放
        if (sx == 1f && sy == 1) {
            return null;
        }

        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 压缩图片到指定文件大小<br/>
     * 算法核心：<br/>
     * 1.先通过计算文件大小与目标大小的比例，缩放成合理的宽高<br/>
     * 2.如果体积还不够小，则通过（循环）减少采样率来减少文件大小<br/>
     *
     * @param imgPath        原图路径
     * @param targetPath     目标路径
     * @param targetByte     目标大小（字节）
     * @param sampleDescOnce 采样率一次减少数量（0-99），默认为10
     * @return 如果不需要压缩，返回null，如果需要压缩则返回目标路径
     */
    public static String compress(String imgPath, String targetPath, long targetByte, int sampleDescOnce) {

        long fileLen = new File(imgPath).length();

        if (fileLen <= targetByte) {//图片小于目标大小，不压缩
            return null;
        }

        //计算图片宽高
        BitmapFactory.Options options = new BitmapFactory.Options();

        //采用合理采样率
        double scale = Math.sqrt((float) fileLen / targetByte);
        options.inSampleSize = (int) Math.ceil(scale);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;

        Bitmap originalBitmap = BitmapFactory.decodeFile(imgPath, options);

        //计算图片的合理宽高
        int fitHeight = (int) (originalBitmap.getHeight() / Math.sqrt(scale));
        int fitWidth = (int) (originalBitmap.getWidth() / Math.sqrt(scale));

        //得到的缩放后的图片
        Bitmap bitmap = scale(originalBitmap, fitWidth, fitHeight);

        if (bitmap != null) {//回收原图
            originalBitmap.recycle();
            //noinspection UnusedAssignment
            originalBitmap = null;
        } else {//使用原图
            bitmap = originalBitmap;
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        int rate = 100;

        do {
            bOut.reset();
            rate -= (sampleDescOnce <= 0 ? 10 : sampleDescOnce);//循环降低质量,默认为10
            if (rate < 0) {//不能再低了，压缩不了
                break;
            }

            //第一个参数 ：图片格式
            //第二个参数： 图片质量，100为最高，0为最差
            //第三个参数：保存压缩后的数据的流
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, bOut);

        }
        while (bOut.size() > targetByte); //如果压缩后大于目标大小，则提高压缩率，重新压缩
        
        //保存图片
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, new FileOutputStream(targetPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return targetPath;
    }


}

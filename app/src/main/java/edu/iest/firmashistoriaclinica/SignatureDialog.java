package edu.iest.firmashistoriaclinica;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignatureDialog {

    private AlertDialog dialog;
    private static LinearLayout mContent;
    private signature mSignature;
    private Button mClear;
    private static Button mGetSign;
    private Button mCancel;
    private View view;
    private static Bitmap bitmap;
    private String ConvertedBitmap;


    // Método que muestra el diálogo de la firma
    public void showDialog(final Context context, final ImageView Firma_xx) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_signature_dialog, null);

        // Cargar la fuente Banhscrift desde la carpeta res/font
        Typeface typeface = ResourcesCompat.getFont(context, R.font.bahnschrift);

        // Crear una vista personalizada para el título con fondo rojo y la fuente Banhscrift
        TextView titleView = new TextView(context);
        titleView.setText("Introduce tu firma");
        titleView.setBackgroundColor(Color.parseColor("#EA0E2C"));
        titleView.setPadding(20, 20, 20, 20);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(18);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(typeface);

        // Establecer la vista personalizada como título del AlertDialog.Builder


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCustomTitle(titleView);

        dialog = builder.create();

        mContent = dialogView.findViewById(R.id.linearLayout);
        mSignature = new signature(context, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        mClear = dialogView.findViewById(R.id.clear);
        mGetSign = dialogView.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = dialogView.findViewById(R.id.cancel);
        view = mContent;

        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSignature.clear();
                bitmap = null;
                Firma_xx.setImageDrawable(null);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String DIRECTORY = context.getExternalFilesDir(null) + "/FIRMAS/";
                String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String StoredPath = DIRECTORY + pic_name + ".png";
                ConvertedBitmap = StoredPath;

                File file = new File(DIRECTORY);
                if (!file.exists()) {
                    file.mkdirs();
                }

                view.setDrawingCacheEnabled(true);
                mSignature.save(view, StoredPath);
                dialog.dismiss();

//                if (Firma_xx.getDrawable() == null) {
////                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
////                    builder.setTitle("Reminder");
////                    builder.setMessage("Please make sure all required fields are not empty before getting the driver's signature.");
////                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////                        }
////                    });
////                    builder.show();
////                } else {
                    File imgFile = new File(ConvertedBitmap);
                    if (imgFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        Firma_xx.setImageBitmap(bitmap);
                    }

                // Reiniciar la variable bitmap
                bitmap = null;

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Firma_xx.getDrawable() == null) {
                    mSignature.clear();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    // Clase interna que representa la vista de la firma
    private static class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();
        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();


        // Configurar la apariencia de la firma
        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }


        // Guardar la firma como una imagen en un directorio específico
        public void save(View v, String StoredPath) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Limpiar la firma
        public void clear() {
            path.reset();
            invalidate();
        }


        // Dibujar la firma en el lienzo del canvas
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;
            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}

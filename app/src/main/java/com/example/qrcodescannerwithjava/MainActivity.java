package com.example.qrcodescannerwithjava;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.qrcodescannerwithjava.Model.QRGeoModel;
import com.example.qrcodescannerwithjava.Model.QRUrlModel;
import com.example.qrcodescannerwithjava.Model.QRVCardModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    private ZXingScannerView scannerView;
    private Button btnMessage;
    private String urlLink;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUIComponents();
        requestPermission();
    }

    @Override
    protected void onResume()
    {
        scannerView.startCamera();
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        scannerView.stopCamera();
        super.onDestroy();
    }

    private void initUIComponents()
    {
        scannerView = findViewById(R.id.scanner_cam_view);
        btnMessage = findViewById(R.id.btnMessage);
        btnMessage.setEnabled(false);
    }

    private void requestPermission()
    {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        Toast.makeText(MainActivity.this, "Camera Permission is required!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @Override
    public void handleResult(Result rawResult)
    {
        processRawResult(rawResult.getText());
    }

    private void processRawResult(String result)
    {
        if (result.startsWith("BEGIN:"))
        {
            String[] tokens = result.split("\n");
            QRVCardModel qrvCardModel = new QRVCardModel();
            btnMessage.setEnabled(false);

            for (String token : tokens)
            {
                if (token.startsWith("BEGIN:"))
                {
                    qrvCardModel.setType(token.substring("BEGIN:".length()));
                }
                else if (token.startsWith("N:"))
                {
                    qrvCardModel.setName(token.substring("N:".length()));
                }
                else if (token.startsWith("ORG:"))
                {
                    qrvCardModel.setOrg(token.substring("ORG:".length()));
                }
                else if (token.startsWith("TEL:"))
                {
                    qrvCardModel.setTel(token.substring("TEL:".length()));
                }
                else if (token.startsWith("URL:"))
                {
                    qrvCardModel.setUrl(token.substring("URL:".length()));
                }
                else if (token.startsWith("EMAIL:"))
                {
                    qrvCardModel.setEmail(token.substring("EMAIL:".length()));
                }
                else if (token.startsWith("ADR:"))
                {
                    qrvCardModel.setAddress(token.substring("ADR:".length()));
                }
                else if (token.startsWith("NOTE:"))
                {
                    qrvCardModel.setNote(token.substring("NOTE:".length()));
                }
                else if (token.startsWith("SUMMARY:"))
                {
                    qrvCardModel.setSummary(token.substring("SUMMARY:".length()));
                }
                else if (token.startsWith("DTSTART"))
                {
                    qrvCardModel.setDtstart(token.substring("DTSTART:".length()));
                }
                else if (token.startsWith("DTEND"))
                {
                    qrvCardModel.setDtend(token.substring("DTEND:".length()));
                }

                btnMessage.setText(qrvCardModel.getType());
            }
        }
        else if (result.startsWith("http://") || result.startsWith("https://") || result.startsWith("www."))
        {
            btnMessage.setEnabled(true);
            QRUrlModel qrUrlModel = new QRUrlModel(result);
            btnMessage.setText(qrUrlModel.getUrl());
            urlLink = qrUrlModel.getUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(urlLink));
            startActivity(i);
        }
        else if (result.startsWith("geo:"))
        {
            btnMessage.setEnabled(false);
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delims = "[ , ?q= ]";
            String[] tokens;
            tokens = result.split(delims);

            for (String token : tokens)
            {
                if (token.startsWith(" geo:"))
                {
                    qrGeoModel.setLat(token.substring("geo:".length()));
                }
            }

            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLat(tokens[1]);
            qrGeoModel.setGeo_place(tokens[2]);

            btnMessage.setText(getString(R.string.MAIN_ACTIVITY_TXT_GEO_MESSAGE, qrGeoModel.getLat(), qrGeoModel.getLng()));//(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());
        }
        else
        {
            btnMessage.setText(result);
        }

        scannerView.resumeCameraPreview(MainActivity.this);
    }

    public void onClicked(View view)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(urlLink));
        startActivity(i);
    }
}

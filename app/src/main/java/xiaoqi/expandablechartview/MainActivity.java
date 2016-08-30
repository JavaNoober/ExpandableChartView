package xiaoqi.expandablechartview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    ChartView chartView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chartView = (ChartView) findViewById(R.id.chartView);
        chartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartView.setScale(360,60,90);
                chartView.show();
            }
        });
    }
}

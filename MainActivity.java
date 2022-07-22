package com.example.mine;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

class myButton extends Button {
    boolean isMine;
    int nei = 0;
    boolean dised = false;

    public myButton(Context context) {
        super(context);
    }

    void Dis() {
        dised = true;
        getBackground().setColorFilter(null);
        if (isMine) {
            getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        } else if (nei != 0) {
            setText(String.valueOf(nei));
            setPadding(0, 0, 0, 0);
        }
    }

}

public class MainActivity extends AppCompatActivity {
    myButton[] but;
    int _row, _col;
    LinearLayout[] li;
    LinearLayout all;
    int clickcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //     setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("扫雷");

        int bcnt = 3;
        all = new LinearLayout(getApplicationContext());
        all.setOrientation(LinearLayout.VERTICAL);
        all.setGravity(Gravity.CENTER);
        Button[] slt = new Button[bcnt];
        String[] text = {"9x9 10雷", "16*16 40雷", "30x16 99雷"};
        for (int i = 0; i != bcnt; ++i) {
            slt[i] = new Button(getApplicationContext());
            slt[i].setText(text[i]);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(500, 300);
            slt[i].getBackground().
                    setColorFilter(Color.parseColor("#7F00FF"),
                            PorterDuff.Mode.MULTIPLY);
            slt[i].setTextSize(25);
            lp.setMargins(50, 50, 50, 50);
            all.addView(slt[i], lp);
        }

        slt[0].setOnClickListener(view -> init(9, 9, 10));
        slt[1].setOnClickListener(view -> init(16, 16, 40));
        slt[2].setOnClickListener(view -> init(16, 30, 99));
        setContentView(all);
    }


    void getNei(int idx, Function<myButton, Boolean> isTrue, Function<Integer, Integer> doSome) {
        int r = idx / _col;
        int c = idx % _col;
        int[] ne = {
                r - 1, c - 1, r - 1, c,
                r - 1, c + 1, r, c - 1,
                r, c + 1, r + 1, c - 1,
                r + 1, c, r + 1, c + 1
        };
        for (int i = 0; i != 8; ++i) {
            if (ne[i * 2] >= 0 && ne[i * 2] < _row &&
                    ne[i * 2 + 1] >= 0 && ne[i * 2 + 1] < _col &&
                    isTrue.apply(but[ne[i * 2] * _col + ne[i * 2 + 1]])) {
                doSome.apply(ne[i * 2] * _col + ne[i * 2 + 1]);
            }
        }
    }

    void isWin() {
        long cntl = Arrays.stream(but).filter((myButton b) -> b.isMine && b.dised).count();
        long cnt = Arrays.stream(but).filter((myButton b) -> !b.isMine && !b.dised).count();
        if (cntl != 0) {
            new AlertDialog.Builder(this).
                    setTitle("失败").setMessage("游戏结束").
                    setPositiveButton("确定", (dialogInterface, i) -> {
                        finish();
                        System.exit(0);
                    }).show();
        } else if (cnt == 0) {
            new AlertDialog.Builder(this).
                    setTitle("成功").setMessage("恭喜你！").
                    setPositiveButton("确定", (dialogInterface, i) -> {
                        finish();
                        System.exit(0);
                    }).show();
        }
    }

    void mkRandMine(int cnt, int off) {
        for (int i = 0; i != cnt; ++i) {
            int idx = new Random().nextInt(_col * _row);

            if (but[idx].isMine) {
                --i;
                continue;
            }
            but[idx].isMine = true;
            but[off].nei = 0;
            getNei(off, (myButton b) -> b.isMine, integer -> but[off].nei++);
            if (but[off].isMine || but[off].nei != 0) {
                but[idx].isMine = false;
                --i;
            }
        }
        for (int i = 0; i != _col * _row; ++i) {
            int finalI = i;
            getNei(i, (myButton b) -> b.isMine, integer -> but[finalI].nei++);
        }
    }

    void init(int ro, int co, int cnt) {
        Objects.requireNonNull(getSupportActionBar()).hide();
        int statusBarHeight = -1;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId != 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        _row = ro;
        _col = co;

        int sWidth = getResources().getDisplayMetrics().widthPixels;
        int sHeight = getResources().getDisplayMetrics().heightPixels;
        int sz = Math.min(sWidth / _row, (sHeight - statusBarHeight) / _col);

        but = new myButton[_col * _row];
        li = new LinearLayout[_row];
        all = new LinearLayout(getApplicationContext());
        all.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i != _row; ++i) {
            li[i] = new LinearLayout(getApplicationContext());
            li[i].setOrientation(LinearLayout.VERTICAL);
        }
        for (int i = 0; i != _col * _row; ++i) {
            but[i] = new myButton(getApplicationContext());
            but[i].getBackground().setColorFilter(Color.parseColor("#7F00FF"), PorterDuff.Mode.MULTIPLY);
            int rw = i / _col;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz + 16, sz + 24);
            lp.setMargins(-8, -12, -8, -12);
            li[rw].addView(but[i], lp);
        }

        for (int i = 0; i != _col * _row; ++i) {
            int finalI = i;
            but[i].setOnClickListener(view -> {
                ++clickcnt;
                if (clickcnt == 1) {
                    mkRandMine(cnt, finalI);
                }
                but[finalI].Dis();
                if (but[finalI].isMine) {
                    for (int i1 = 0; i1 != _col * _row; ++i1) {
                        but[i1].Dis();
                    }
                } else if (but[finalI].nei == 0) {
                    getNei(finalI, (myButton b) -> !b.dised, (Integer i12) -> {
                        but[i12].callOnClick();
                        return 0;
                    });
                }
                isWin();
            });
        }
        for (int i = 0; i != _row; ++i) {
            all.addView(li[i]);
        }

        setContentView(all);
    }
}
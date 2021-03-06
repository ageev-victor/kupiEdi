package ru.ageev_victor.kupiedi.Activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;

import ru.ageev_victor.kupiedi.Objects.DataFromDataBase;
import ru.ageev_victor.kupiedi.Objects.DatabaseHelper;
import ru.ageev_victor.kupiedi.Objects.Finder;
import ru.ageev_victor.kupiedi.Objects.Row;
import ru.ageev_victor.kupiedi.R;

public class MainActivity extends AppCompatActivity {

    Button btnAddFood;
    Button foodBtn1;
    Button foodBtn2;
    Button foodBtn3;
    EditText edTxtEnterFood;
    LinearLayout buttonsLayout;
    RelativeLayout mainLayout;
    private Finder finder;
    Toolbar toolbar;
    protected static ArrayList<Row> rows = new ArrayList<>();
    public static Typeface defaultTypeface;
    public static int defaultTextSize;
    static TableLayout tableListFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finder = new Finder(this);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        setTypeFace();
        setSettingsToTableListFood();
        setSupportActionBar(toolbar);
        setupFloatingButton();
        addOnClickListenersToButtons();
        addOnClickListenerToEditText();
    }

    private void initViews() {
        foodBtn1 = (Button) findViewById(R.id.btnFood1);
        foodBtn2 = (Button) findViewById(R.id.btnFood2);
        foodBtn3 = (Button) findViewById(R.id.btnFood3);
        btnAddFood = (Button) findViewById(R.id.btnAddFood);
        edTxtEnterFood = (EditText) findViewById(R.id.edTxtEnterFood);
        tableListFood = (TableLayout) findViewById(R.id.tableListFood);
        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setTypeFace() {
        defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "bauhauslightctt_bold.ttf");
        edTxtEnterFood.setTypeface(defaultTypeface);
    }

    private void setSettingsToTableListFood() {
        assert tableListFood != null;
        tableListFood.setShrinkAllColumns(true);
        tableListFood.setColumnStretchable(0, true);
        tableListFood.setColumnStretchable(1, true);
    }

    private void setupFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableListFood.removeAllViews();
                rows.clear();
                if (mainLayout.getChildCount() == 3) {
                    edTxtEnterFood.setText("");
                    mainLayout.removeView(buttonsLayout);
                }
                Snackbar.make(view, R.string.list_cleared, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadTable("tempTable");
        setFontSize();
        setFontStyle();
        DatabaseHelper.getInstance(this).deleteTable("tempTable");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!(rows.isEmpty())) {
            saveList("tempTable");
            rows.clear();
        }

    }

    private void setFontSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int defaultTextSize_temp = (int) Float.parseFloat(prefs.getString(getString(R.string.settings), "16"));
        if (defaultTextSize_temp < 12 | defaultTextSize_temp > 26) {
            Toast.makeText(this, R.string.max_font_size_warning, Toast.LENGTH_SHORT).show();
        } else {
            defaultTextSize = defaultTextSize_temp;
            for (Row row : rows) {
                row.getTxtRowFoodName().setTextSize(defaultTextSize);
                row.getEdTxtFoodCunt().setTextSize(defaultTextSize);
            }
        }

    }


    private void setFontStyle() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String font = prefs.getString(getString(R.string.font_style), "");
        if (font.contains(getString(R.string.amoderno)))
            defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "amoderno.ttf");
        if (font.contains(getString(R.string.aalbionic_bold)))
            defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "aalbionic_bold.ttf");
        if (font.contains(getString(R.string.aalternanr)))
            defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "aalternanr.ttf");
        if (font.contains(getString(R.string.bauhauslightctt_bold)))
            defaultTypeface = Typeface.createFromAsset(getResources().getAssets(), "bauhauslightctt_bold.ttf");
        for (Row row : rows) {
            edTxtEnterFood.setTypeface(defaultTypeface);
            row.getTxtRowFoodName().setTypeface(defaultTypeface);
            row.getEdTxtFoodCunt().setTypeface(defaultTypeface);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final EditText dialogSaveEdText = new EditText(this);
        final CharSequence[] saveLists = DatabaseHelper.getInstance(this).loadList();
        switch (item.getItemId()) {
            case R.id.savelist:
                saveListAlertDialog(dialogSaveEdText, saveLists);
                break;
            case R.id.loadList:
                final CharSequence[] loadLists = DatabaseHelper.getInstance(this).loadList();
                if (loadLists.length > 0) {
                    loadListAlertDialog(loadLists);
                } else {
                    Toast.makeText(this, R.string.no_saved_lists, Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.settings:
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.about:
                Intent aboutIntent = new Intent();
                aboutIntent.setClass(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveListAlertDialog(final EditText dialogSaveEdText, final CharSequence[] saveLists) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Создать новый список или выбрать существующий?")
                .setPositiveButton(getString(R.string.createNewList), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        createListAlertDialog(dialogSaveEdText);
                    }
                })
                .setNegativeButton(R.string.chooseExistList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (saveLists.length > 0) {
                            chooseExistListAlertDialog(saveLists);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.no_saved_lists, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .create()
                .show();
    }

    private void loadListAlertDialog(final CharSequence[] loadLists) {
        new AlertDialog.Builder(this)
                .setItems(loadLists, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tableListFood.removeAllViews();
                        loadTable(loadLists[i].toString());
                        Toast.makeText(getApplicationContext(), R.string.list_is_load, Toast.LENGTH_SHORT).show();
                    }
                })
                .create()
                .show();
    }

    private void chooseExistListAlertDialog(final CharSequence[] saveLists) {
        new AlertDialog.Builder(MainActivity.this)
                .setItems(saveLists, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            saveList(String.valueOf(saveLists[i]));
                            Toast.makeText(getApplicationContext(), R.string.list_save, Toast.LENGTH_SHORT).show();
                        } catch (RuntimeException r) {
                            Toast.makeText(getApplicationContext(), R.string.name_must_contain_letters, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create()
                .show();
    }

    private void createListAlertDialog(final EditText dialogSaveEdText) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.enter_name_list))
                .setView(dialogSaveEdText)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            saveList(dialogSaveEdText.getText().toString());
                            Toast.makeText(getApplicationContext(), R.string.list_save, Toast.LENGTH_SHORT).show();
                        } catch (RuntimeException r) {
                            Toast.makeText(getApplicationContext(), R.string.name_must_contain_letters, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .create()
                .show();
    }


    private void addOnClickListenerToEditText() {
        edTxtEnterFood.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                finder.foodMatches.clear();
                doAllButtonsInvisible();
                ArrayList<String> foodMatches = finder.getMatches(s);
                if (s.length() == 0 || (mainLayout.getChildCount() > 2)) {
                    mainLayout.removeView(buttonsLayout);
                }
                switch (foodMatches.size()) {
                    case 1: {
                        mainLayout.addView(buttonsLayout);
                        foodBtn1.setVisibility(View.VISIBLE);
                        foodBtn1.setText(foodMatches.get(0));
                        break;
                    }
                    case 2: {
                        mainLayout.addView(buttonsLayout);
                        foodBtn1.setText(foodMatches.get(0));
                        foodBtn2.setText(foodMatches.get(1));
                        foodBtn1.setVisibility(View.VISIBLE);
                        foodBtn2.setVisibility(View.VISIBLE);
                        break;
                    }
                }
                if (foodMatches.size() >= 3 & foodMatches.size() < finder.foodNames.size()) {
                    mainLayout.addView(buttonsLayout);
                    foodBtn1.setText(foodMatches.get(0));
                    foodBtn2.setText(foodMatches.get(1));
                    foodBtn3.setText(foodMatches.get(2));
                    doAllButtonsVisible();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void doAllButtonsVisible() {
        foodBtn1.setVisibility(View.VISIBLE);
        foodBtn2.setVisibility(View.VISIBLE);
        foodBtn3.setVisibility(View.VISIBLE);
    }

    private void doAllButtonsInvisible() {
        foodBtn1.setVisibility(View.INVISIBLE);
        foodBtn2.setVisibility(View.INVISIBLE);
        foodBtn3.setVisibility(View.INVISIBLE);
    }

    private void addOnClickListenersToButtons() {
        foodBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Row row = new Row(getApplicationContext(), foodBtn1.getText().toString());
                setOnClickListenerToButton(row);
            }
        });
        foodBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Row row = new Row(getApplicationContext(), foodBtn2.getText().toString());
                setOnClickListenerToButton(row);

            }
        });
        foodBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Row row = new Row(getApplicationContext(), foodBtn3.getText().toString());
                setOnClickListenerToButton(row);

            }
        });
    }

    private void setOnClickListenerToButton(Row row) {
        rows.add(row);
        tableListFood.addView(row);
        edTxtEnterFood.setText("");
        doAllButtonsInvisible();
        mainLayout.removeView(buttonsLayout);
    }

    public static void removeRow(TableRow row) {
        tableListFood.removeView(row);
        rows.remove(row);
    }

    public void saveList(final String tableName) {
        DatabaseHelper.getInstance(this).deleteTable(tableName);
        DatabaseHelper.getInstance(this).createTable(tableName);
        for (final Row row : rows) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.PRODUCT_NAME, (String) row.getTxtRowFoodName().getText());
                    values.put(DatabaseHelper.PRODUCT_COUNT, row.getEdTxtFoodCunt().getText().toString());
                    DatabaseHelper.getInstance(getApplicationContext()).getDataBase().insert(tableName, null, values);
                }
            });
            thread.start();
        }
    }

    public void addFoodToTable(View view) {
        String foodname = edTxtEnterFood.getText().toString();
        if (!(foodname.equals("") | foodname.equals(" "))) {
            Row row = new Row(this, edTxtEnterFood.getText().toString());
            rows.add(row);
            tableListFood.addView(row);
            edTxtEnterFood.setText("");
            mainLayout.removeView(buttonsLayout);
        }
    }

    private void loadTable(String list) {
        try {
            tableListFood.removeAllViews();
            rows.clear();
            ArrayList<DataFromDataBase> dataArray = DatabaseHelper.getInstance(this).getData(list);
            for (DataFromDataBase dataRow : dataArray) {
                Row row = new Row(this, dataRow.getProductName());
                setFontStyle();
                setFontSize();
                row.setFoodCount(dataRow.getProductCount());
                tableListFood.addView(row);
                rows.add(row);
            }
            dataArray.clear();
        } catch (SQLiteException e) {
            if (!e.getMessage().contains("tempTable")) {
                Toast.makeText(this, getString(R.string.error_load_list_from_db) + list, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void delListFromDataBase(MenuItem item) {
        final CharSequence[] lists = DatabaseHelper.getInstance(this).loadList();
        if (lists.length > 0) {
            new AlertDialog.Builder(this)
                    .setItems(lists, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseHelper.getInstance(getApplicationContext()).deleteTable(String.valueOf(lists[i]));
                            Toast.makeText(getApplicationContext(), R.string.list_delete, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        } else {
            Toast.makeText(this, R.string.no_saved_lists, Toast.LENGTH_LONG).show();
        }
    }
}


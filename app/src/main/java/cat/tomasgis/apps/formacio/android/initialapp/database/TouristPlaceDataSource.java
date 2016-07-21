package cat.tomasgis.apps.formacio.android.initialapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Iterator;
import java.util.LinkedHashMap;

import cat.tomasgis.apps.formacio.android.initialapp.interfaces.ITouristDataAccess;
import cat.tomasgis.apps.formacio.android.initialapp.model.TouristPlaceModel;

/**
 * Created by TomasGiS on 20/7/16.
 */
public class TouristPlaceDataSource implements  ITouristDataAccess {

    final String[] FIELDS = {DataContract.TouristPlace._ID,
            DataContract.TouristPlace.TITLE,
            DataContract.TouristPlace.DESCRIPTION,
            DataContract.TouristPlace.APERTURE_TIME,
            DataContract.TouristPlace.PLACE,
            DataContract.TouristPlace.PRICE,
            DataContract.TouristPlace.LOCATION_LAT,
            DataContract.TouristPlace.LOCATION_LON,
            DataContract.TouristPlace.IMAGE_URL,
            DataContract.TouristPlace.RATING};
    private final String TAG = TouristPlaceDataSource.class.getSimpleName();

    // Database fields
    private SQLiteDatabase database = null;
    private TouristicSQLHelper dbHelper;

    private static TouristPlaceDataSource instance;


    private TouristPlaceDataSource(){};


    public static TouristPlaceDataSource getInstance(Context context)
    {

        if (instance == null)
        {
            instance = new TouristPlaceDataSource(context);
        }

        return instance;
    }

    /**
     * The constructor creates a connection with the DataBase Delegate ({@link TouristicSQLHelper})
     * @param context application or activity context
     */
    public TouristPlaceDataSource(Context context) {
        dbHelper = new TouristicSQLHelper(context);
    }

    /***
     * Toggle write access to Tourist database through the class {@link TouristicSQLHelper}
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public boolean isOpen()
    {
        return (database != null);
    }

    /**
     * Close database access
     */
    public void close() {
        dbHelper.close();
    }

    public long create(TouristPlaceModel touristPlace) {

        if (!isOpen()) this.open();

        ContentValues values = new ContentValues();
        values.put(DataContract.TouristPlace.TITLE, touristPlace.getTitle());
        values.put(DataContract.TouristPlace.DESCRIPTION, touristPlace.getDescription());
        values.put(DataContract.TouristPlace.APERTURE_TIME, touristPlace.getApertureTime());
        values.put(DataContract.TouristPlace.PLACE, touristPlace.getPlace());
        values.put(DataContract.TouristPlace.PRICE, touristPlace.getPrice());

        LatLng location = touristPlace.getLocation();
        values.put(DataContract.TouristPlace.LOCATION_LAT,location.latitude);
        values.put(DataContract.TouristPlace.LOCATION_LON,location.longitude);

        values.put(DataContract.TouristPlace.IMAGE_URL, touristPlace.getImageURL());
        values.put(DataContract.TouristPlace.RATING, touristPlace.getRating());

        long insertId = database.insert(TouristicSQLHelper.TABLE_PLACES, null, values);

        return insertId;
    }

    public TouristPlaceModel query(String title)
    {

        if (!isOpen()) this.open();


        Cursor cursor = database.query(TouristicSQLHelper.TABLE_PLACES,
                FIELDS, DataContract.TouristPlace.TITLE + " = \'" + title+"\'", null,
                null, null, null);

        TouristPlaceModel touristPlaceModel = cursorToPlaceModel(cursor);

        return touristPlaceModel;
    }

    public int delete(TouristPlaceModel touristPlaceModel) {
        return this.delete(touristPlaceModel.getTitle());
    }

    public int delete(String title) {
        if (!isOpen()) this.open();


        System.out.println("TouristPlaceModel deleted with title: " + title);
        int id = database.delete(TouristicSQLHelper.TABLE_PLACES, DataContract.TouristPlace.TITLE
                + " = \'" + title+"\'", null);
        return id;
    }

    private TouristPlaceModel cursorToPlaceModel(Cursor cursor) {

        cursor.moveToFirst();

        LatLng location = new LatLng(cursor.getDouble(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.LOCATION_LAT)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.LOCATION_LON)));

        TouristPlaceModel touristPlaceModel = new TouristPlaceModel(
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.APERTURE_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.PLACE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.PRICE)),
                location,
                cursor.getString(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.IMAGE_URL)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(DataContract.TouristPlace.RATING))
        );

        return touristPlaceModel;
    }

    public LinkedHashMap<String,TouristPlaceModel> getAllTouristPlaces() {

        if(!isOpen()) this.open();

        LinkedHashMap<String,TouristPlaceModel> touristPlacelist = new LinkedHashMap<String,TouristPlaceModel>();

        Cursor cursor = database.query(TouristicSQLHelper.TABLE_PLACES,
                FIELDS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TouristPlaceModel touristPlaceModel = cursorToPlaceModel(cursor);
            touristPlacelist.put(touristPlaceModel.getTitle(),touristPlaceModel);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return touristPlacelist;
    }


    @Override
    public boolean clearData() {

        if(!this.isOpen()) this.open();
        instance.database.execSQL("DROP TABLE IF EXISTS " + TouristicSQLHelper.TABLE_PLACES);
        instance.dbHelper.onCreate(instance.database);
        return false;
    }

    @Override
    public String[] getTitles() {

        if(!isOpen()) this.open();

        Cursor cursor = database.query(TouristicSQLHelper.TABLE_PLACES,
                FIELDS, null, null, null, null, null);

        String titles[] = new String[cursor.getCount()];
        int index=0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TouristPlaceModel touristPlaceModel = cursorToPlaceModel(cursor);
            titles[index++] = touristPlaceModel.getTitle();
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return titles;
    }

    @Override
    public int getNumberOfPlaces() {
        Cursor cursor = database.query(TouristicSQLHelper.TABLE_PLACES,
                FIELDS, null, null, null, null, null);
        return cursor.getCount();
    }

    @Override
    public TouristPlaceModel getTouristPlaceModel(String key) {

        return this.query(key);
    }

    @Override
    public boolean addTouristPlace(TouristPlaceModel touristPlaceModel) {

        long id = this.create(touristPlaceModel);

        return (id>=0);
    }

    public Cursor getIterableCursor()
    {
        Cursor cursor = database.query(TouristicSQLHelper.TABLE_PLACES,
                FIELDS, null, null, null, null, null);
        return cursor;
    }
}
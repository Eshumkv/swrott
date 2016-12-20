package be.thomasmore.swrott;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import be.thomasmore.swrott.data.DatabaseHelper;
import be.thomasmore.swrott.data.HttpReader;
import be.thomasmore.swrott.data.JSONHelper;
import be.thomasmore.swrott.data.People;
import be.thomasmore.swrott.data.Planet;
import be.thomasmore.swrott.data.RootsReader;
import be.thomasmore.swrott.data.Species;

public class Splash extends AppCompatActivity {

    private final int SPLASH_WAIT_TIME = 3000;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get a reference to the database
        db = new DatabaseHelper(this);

        // If we don't have internet, what then??!
        if (!Helper.isInternetAvailable()) {
            // No internet :(

            // See if we have a few items in the db
            // If we have, we probably updated
            if (db.getPlanetsCount() > 20) {
                goToMain();
                return;
            }

            // There is not enough data, maybe load the old data?
            // If not, there's nothing we can do.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setMessage(R.string.dialog_no_internet)
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Quit
                        Splash.this.finish();
                        System.exit(0);
                    }
                })
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Load the old data
                        List<Planet> planets = Helper.deserializeObj(getApplicationContext(), "planets.ser", true);
                        List<Species> species = Helper.deserializeObj(getApplicationContext(), "species.ser", true);
                        List<People> people = Helper.deserializeObj(getApplicationContext(), "people.ser", true);

                        db.deleteAllPlanets();
                        db.deleteAllSpecies();
                        db.deleteAllPeople();

                        db.insertPlanets(planets);
                        db.insertSpecies(species);
                        db.insertPeople(people);

                        goToMain();
                    }
                });
            builder.create().show();

            return;
        }

        // Create a loading spinner
        FragmentManager fm = getSupportFragmentManager();
        final MySpinnerDialog spinner = new MySpinnerDialog();
        spinner.show(fm, "some_tag");

        // Create a list to hold results
        // Hacky way of getting data from a thread
        final int listSize = 3 * 2;     // 3 threads (which might call another thread)
        final List<Boolean> results = new ArrayList<>(listSize);

        // Make the thread to check whether we've loaded
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (results.size() < listSize) {
                    SystemClock.sleep(500); // NEVER on UI-THREAD!!!!
                }

                // We're done loading
                spinner.dismiss();
                goToMain();
            }
        }).start();

        final String planetUrl = "http://swapi.co/api/planets/";
        new HttpReader(new HttpReader.OnResultReadyListener() {
            @Override
            public void resultReady(String result) {
                int apiCount = JSONHelper.getCount(result);
                int dbPlanetCount = db.getPlanetsCount();

                if (apiCount != dbPlanetCount) {
                    // Database is not synced
                    new RootsReader<>(JSONHelper.JSONTypes.Planet, new RootsReader.OnResultReadyListener() {
                        @Override
                        public void resultReady(List result) {
                            List<Planet> planets = (List<Planet>)result;
                            db.deleteAllPlanets();
                            db.insertPlanets(planets);
                            results.add(true);
                        }
                    }).execute(planetUrl);
                } else {
                    // DB is *probably* synced
                    results.add(true);
                }

                // Done
                results.add(true);
            }
        }).execute(planetUrl);

        final String peopleUrl = "http://swapi.co/api/people/";
        new HttpReader(new HttpReader.OnResultReadyListener() {
            @Override
            public void resultReady(String result) {
                int apiCount = JSONHelper.getCount(result);
                int dbCount = db.getPeopleCount();

                if (apiCount != dbCount) {
                    // Database is not synced
                    new RootsReader<>(JSONHelper.JSONTypes.People, new RootsReader.OnResultReadyListener() {
                        @Override
                        public void resultReady(List result) {
                            List<People> people = (List<People>)result;
                            db.deleteAllPeople();
                            db.insertPeople(people);
                            results.add(true);
                        }
                    }).execute(peopleUrl);
                } else {
                    // DB is *probably* synced
                    results.add(true);
                }

                // Done
                results.add(true);
            }
        }).execute(peopleUrl);

        final String speciesUrl = "http://swapi.co/api/species/";
        new HttpReader(new HttpReader.OnResultReadyListener() {
            @Override
            public void resultReady(String result) {
                int apiCount = JSONHelper.getCount(result);
                int dbCount = db.getSpeciesCount();

                if (apiCount != dbCount) {
                    // Database is not synced
                    new RootsReader<>(JSONHelper.JSONTypes.Species, new RootsReader.OnResultReadyListener() {
                        @Override
                        public void resultReady(List result) {
                            List<Species> species = (List<Species>)result;
                            db.deleteAllSpecies();
                            db.insertSpecies(species);
                            results.add(true);
                        }
                    }).execute(speciesUrl);
                } else {
                    // DB is *probably* synced
                    results.add(true);
                }

                // Done
                results.add(true);
            }
        }).execute(speciesUrl);

        // Handler to start the MainActivity
        // Close this splash after specified time
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent mainIntent = new Intent(Splash.this, MainActivity.class);
//                Splash.this.startActivity(mainIntent);
//                Splash.this.finish();
//            }
//        }, SPLASH_WAIT_TIME);
    }

    private void goToMain() {
        Intent mainIntent = new Intent(Splash.this, MainActivity.class);
        Splash.this.startActivity(mainIntent);
        Splash.this.finish();
    }
}

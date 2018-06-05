package com.mitchlthompson.mealqueue;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    //Add starter data to user then start MainActivity
                                    AddStarterRecipes();
                                    //startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void AddStarterRecipes(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/recipes/" + userID);

        String key1 = mRef.push().getKey();
        mRef.child(key1).child("Recipe Name").setValue("Apple and Cabbage Salad");
        mRef.child(key1).child("Directions").setValue("Using a mandoline or sharp knife, thinly slice the cabbage, apple, " +
                "and red onion to resemble extremely fine confetti; transfer to a large bowl.\n\n" +
                "Add the walnuts and raisins. Toss gently to combine.\n\n" +
                "In a small bowl, whisk together all the ingredients for the dressing.\n\n" +
                "Top the salad with the dressing. Using salad tongs, mix until evenly dressed.");
        HashMap<String, String> ingredients1 = new HashMap<>();
        ingredients1.put("Savoy cabbage", "1/2 head");
        ingredients1.put("red cabbage", "1/2 head");
        ingredients1.put("Fuji apple", "1");
        ingredients1.put("red onion", "1/4");
        ingredients1.put("walnuts", "1/2 cup");
        ingredients1.put("golden raisins", "1/2 cup");
        ingredients1.put("apple cider vinegar", "2 tablespoons");
        ingredients1.put("cayenne pepper", "1/8 teaspoon");
        ingredients1.put("nonfat Greek yogurt", "1/3 cup");
        ingredients1.put("agave nectar", "2 teaspoons");
        ingredients1.put("fennel seeds", "1 teaspoon");
        mRef.child(key1).child("Ingredients").setValue(ingredients1);
        mRef.child(key1).child("Recipe ID").setValue(key1);


        String key2 = mRef.push().getKey();
        mRef.child(key2).child("Recipe Name").setValue("Cheddar-Cauliflower Soup");
        mRef.child(key2).child("Directions").setValue("Heat the oil in a large saucepan over medium heat. Add the leeks and stir until very soft, about five minutes.\n\n" +
                "Add cauliflower, two cups milk, water, bay leaf, and salt and pepper. Bring to a boil over medium-high heat, stirring often.\n\n" +
                "Reduce heat to a simmer, cover, and cook, stirring occasionally, until the cauliflower is soft, about eight minutes.\n\n" +
                "Meanwhile, whisk the remaining 1/2 cup milk and flour in a small bowl.\n\n" +
                "When the cauliflower is soft, remove the bay leaf and stir in the milk mixture. Cook over medium-high heat, stirring, until the soup has thickened slightly, about two minutes more.\n\n" +
                "Remove from the heat. Stir in cheese and lemon juice.");
        HashMap<String, String> ingredients2 = new HashMap<>();
        ingredients2.put("extra-virgin olive oil", "2 tablespoons");
        ingredients2.put("leeks", "2 large");
        ingredients2.put("cauliflower florets", "4 cups");
        ingredients2.put("low-fat milk", "2 1/2 cups");
        ingredients2.put("water", "2 cups");
        ingredients2.put("bay leaf", "1");
        ingredients2.put("salt", "1 teaspoon");
        ingredients2.put("white or black pepper", "1/2 teaspoon");
        ingredients2.put("all-purpose flour", "2 tablespoons");
        ingredients2.put("cheddar cheese", "1 1/2 cups");
        ingredients2.put("lemon juice", "1 tablespoon");
        mRef.child(key2).child("Ingredients").setValue(ingredients1);
        mRef.child(key2).child("Recipe ID").setValue(key2);

        String key4 = mRef.push().getKey();
        mRef.child(key4).child("Recipe Name").setValue("Burrito Bowl");
        mRef.child(key4).child("Directions").setValue("Microwave black beans with chicken broth, " +
                "oregano, cumin, cayenne, and garlic powder on high for 30 to 45 seconds until heated. Set aside.\n\n" +
                "Add red cabbage to your bowl, and spoon the black beans on top. Layer sliced chicken, " +
                "Greek yogurt, salsa, and cilantro and green onions, and enjoy immediately!");
        HashMap<String, String> ingredients4 = new HashMap<>();
        ingredients4.put("black beans", "1/2 cup");
        ingredients4.put("chicken breast,", "3 ounces");
        mRef.child(key4).child("Ingredients").setValue(ingredients4);
        mRef.child(key4).child("Recipe ID").setValue(key4);

        String key5 = mRef.push().getKey();
        mRef.child(key5).child("Recipe Name").setValue("Zucchini Boats");
        mRef.child(key5).child("Directions").setValue("Preheat the oven to 450° F.\n\n" +
                "Wash each zucchini thoroughly. Cut off the ends and then cut each one in half. Then " +
                "cut each half lengthwise so you have 12 pieces of zucchini total.\n\n" +
                "With a spoon or paring knife, scoop out the seeds and compost them or use them to " +
                "make soup broth. Place the zucchini boats on a lightly oiled cookie sheet or Silpat.\n\n" +
                "To make the filling, chop the basil and place it in a bowl with the garlic, grated" +
                " carrot, ricotta, parmesan, lemon juice, salt, and pepper. Mix it thoroughly.\n\n" +
                "Fill each boat with the creamy mixture. Bake for 30 to 40 minutes or until the zucchini is tender.\n\n" +
                "Serve warm and enjoy.");
        HashMap<String, String> ingredients5 = new HashMap<>();
        ingredients5.put("zucchinis", "3");
        ingredients5.put("minced garlic", "1 clove");
        mRef.child(key5).child("Ingredients").setValue(ingredients5);
        mRef.child(key5).child("Recipe ID").setValue(key5);

        String key6 = mRef.push().getKey();
        mRef.child(key6).child("Recipe Name").setValue("Honey-Garlic Shrimp");
        mRef.child(key6).child("Directions").setValue("Whisk the honey, soy sauce, garlic, and ginger " +
                "(if using) together in a medium bowl.\n\n" +
                "Place shrimp in a large zipped-top bag or tupperware. Pour 1/2 of the marinade " +
                "mixture on top, give it all a shake or stir, then allow shrimp to marinate in the " +
                "refrigerator for 15 minutes or for up to 8-12 hours. Cover and refrigerate the rest of " +
                "the marinade for step 3. (Time-saving tip: while the shrimp is marinating, " +
                "I steamed broccoli and microwaved some quick brown rice.)\n\n" +
                "Heat olive oil in a skillet over medium-high heat. Place shrimp in the skillet. " +
                "(Discard used marinade2.) Cook shrimp on one side until pink-- about 45 seconds-- " +
                "then flip shrimp over. Pour in remaining marinade and cook it all until shrimp is cooked through, about 1 minute more.\n\n" +
                "Serve shrimp with cooked marinade sauce and a garnish of green onion. The sauce is excellent on brown rice and steamed veggies on the side.");
        HashMap<String, String> ingredients6 = new HashMap<>();
        ingredients6.put("honey", "1/2 cup");
        ingredients6.put("soy sauce", "1/4 cup");
        ingredients6.put("minced garlic", "1 tablespoon");
        ingredients6.put("fresh  minced ginger", "1 teaspoon");
        ingredients6.put("uncooked shrimp", "1 lb");
        ingredients6.put("olive oil", "2 teaspoons");
        mRef.child(key6).child("Ingredients").setValue(ingredients6);
        mRef.child(key6).child("Recipe ID").setValue(key6);

        String key7 = mRef.push().getKey();
        mRef.child(key7).child("Recipe Name").setValue("Beef and Broccoli");
        mRef.child(key7).child("Directions").setValue("Toss sliced beef in a large bowl with corn starch.\n\n" +
                "Heat canola oil in a pan over medium heat for a few minutes. Add sliced beef and cook until it browns, a few minutes, stirring frequently. Transfer to a plate and set aside.\n\n" +
                "Add broccoli and garlic to the pan, and stir. Add beef broth. Let simmer until the broccoli is tender, about 10 minutes, stirring occasionally.\n\n" +
                "While waiting for the broccoli to cook, combine all of the sauce ingredients in a bowl and mix well.\n\n" +
                "Add the reserved beef and sauce to the pan, and stir. Let simmer for 5 minutes so the sauce thickens a bit.\n\n" +
                "Serve beef and broccoli over cooked white rice.");
        HashMap<String, String> ingredients7 = new HashMap<>();
        ingredients7.put("flank steak", "1 pound");
        ingredients7.put("broccoli florets", "3 cups");
        mRef.child(key7).child("Ingredients").setValue(ingredients7);
        mRef.child(key7).child("Recipe ID").setValue(key7);

        String key8 = mRef.push().getKey();
        mRef.child(key8).child("Recipe Name").setValue("Tomato Spinach Spaghetti");
        mRef.child(key8).child("Directions").setValue("Add chopped sun-dried tomatoes and 2 tablespoons of olive oil, drained from sun-dried tomatoes, to a large skillet, on medium-low heat. Add chopped chicken (I used boneless skinless chicken thighs and prefer to use them, but you can use chopped chicken breast, as well), red pepper flakes, and salt over all of the ingredients in the skillet, and cook on medium heat until chicken is cooked through and no longer pink, about 5 minutes.\n\n" +
                "Add chopped tomatoes, chopped fresh basil leaves, fresh spinach, and chopped garlic to the skillet with chicken, cook on medium heat about 3- 5 minutes until spinach wilts just a little, and tomatoes release some of their juices. Remove from heat. Taste, and add more salt to taste, if needed. Cover with lid and keep off heat.\n\n" +
                "Cook pasta according to package instructions, until al dente. Drain, and add cooked and drained pasta to the skillet with the chicken and vegetables. Reheat on low heat, mix everything well, add more seasonings (salt and pepper), if desired. Remove from heat.\n\n" +
                "At this point, when the pasta and vegetables are off heat, you can add more high quality olive oil, which is really tasty. Or you can add more olive oil from the jar from the sun-dried tomatoes.");
        HashMap<String, String> ingredients8 = new HashMap<>();
        ingredients8.put("sun-dried tomatoes", "1/2 cup");
        ingredients8.put("spaghetti pasta", "8 oz");
        mRef.child(key8).child("Ingredients").setValue(ingredients8);
        mRef.child(key8).child("Recipe ID").setValue(key8);

        String key9 = mRef.push().getKey();
        mRef.child(key9).child("Recipe Name").setValue("Garlic-Parmesan Crusted Salmon");
        mRef.child(key9).child("Directions").setValue("Preheat oven to 400 F. Line baking sheet with parchment paper (make sure the parchment paper packaging says that it's safe to use at 400 F).\n\n" +
                "Pat dry salmon. Brush with 2 tablespoons of olive oil from all sides. Season with salt and pepper. Place the salmon, skin side down, on a parchment paper lined baking sheet. Coat asparagus with olive oil, season with salt and pepper, and place around salmon on a baking sheet.\n\n" +
                "Spread minced garlic on top of the salmon and the asparagus. Top with grated Parmesan cheese.\n\n" +
                "Bake the salmon in the preheated oven at 400 F for 15-20 minutes.\n\n" +
                "\n\n" +
                "Remove from the oven, top with chopped fresh parsley before serving.");
        HashMap<String, String> ingredients9 = new HashMap<>();
        ingredients9.put("salmon", "1/2 lb");
        ingredients9.put("asparagus", "1 lb");
        mRef.child(key9).child("Ingredients").setValue(ingredients9);
        mRef.child(key9).child("Recipe ID").setValue(key9);

        String key10 = mRef.push().getKey();
        mRef.child(key10).child("Recipe Name").setValue("Breakfast Egg Muffins");
        mRef.child(key10).child("Directions").setValue("Preheat the oven to 350°F. Grease a muffin pan with cooking spray.\n" +
                "\n" +
                "In a large bowl, whisk together the eggs, nonfat milk and 1/2 teaspoon pepper. Stir in the spinach, tomatoes and onions.\n" +
                "\n" +
                "Divide the mixture evenly between the 12 muffin pan cups and bake the muffins for 20 to 25 minutes, or until the egg is fully cooked. Remove the muffins from the oven and let them cool for 5 minutes in the pan then use a knife to loosen the muffins from the cups.\n" +
                "\n" +
                "Top each muffin with sliced avocado, a dollop of salsa and a sprinkling of cheese then serve.");
        HashMap<String, String> ingredients10 = new HashMap<>();
        ingredients10.put("eggs", "12");
        ingredients10.put("nonfat milk", "1/4 cup");
        ingredients10.put("fresh spinach", "1 cup");
        ingredients10.put("cherry tomatoes", "3/4 cup");
        ingredients10.put("onions", "1/2 cup");
        ingredients10.put("avocado", "1");
        ingredients10.put("Salsa", "1/2 cup");
        mRef.child(key10).child("Ingredients").setValue(ingredients10);
        mRef.child(key10).child("Recipe ID").setValue(key10);

        String key11 = mRef.push().getKey();
        mRef.child(key11).child("Recipe Name").setValue("Tuna-Stuffed Avocado Boats");
        mRef.child(key11).child("Directions").setValue("Scoop out some of the avocado from the pitted area to widen the \"bowl\" area. Place the scooped avocado into a medium-size mixing bowl. Mash it with a fork.\n\n" +
                "Add the tuna, bell pepper, jalapeno, and cilantro to the mixing bowl. Pour lime juice over. Stir it all together until everything is well mixed.\n\n" +
                "Scoop the tuna into the avocado bowls. Season with salt and pepper.");
        HashMap<String, String> ingredients11 = new HashMap<>();
        ingredients11.put("avocado", "1");
        ingredients11.put("tuna", " 1 (4.5oz) can");
        mRef.child(key11).child("Ingredients").setValue(ingredients11);
        mRef.child(key11).child("Recipe ID").setValue(key11);

        String key12 = mRef.push().getKey();
        mRef.child(key12).child("Recipe Name").setValue("Fat Head Pizza");
        mRef.child(key12).child("Directions").setValue("Mix the shredded/grated cheese and almond flour/meal in a microwaveable bowl. Add the cream cheese. Microwave on HIGH for 1 minute.\n" +
                "Stir then microwave on HIGH for another 30 seconds.\n\n" +
                "Add the egg, salt, rosemary and any other flavourings, mix gently.\n\n" +
                "Place in between 2 pieces of baking parchment/paper and roll into a circular pizza shape (see photos above). Remove the top baking paper/parchment. If the mixture hardens and becomes difficult to work with, pop it back in the microwave for 10-20 seconds to soften again but not too long or you will cook the egg.\n\n" +
                "Make fork holes all over the pizza base to ensure it cooks evenly.\n\n" +
                "Slide the baking paper/parchment with the pizza base, on a baking tray (cookie tray) or pizza stone, and bake at 220C/425F for 12-15 minutes, or until brown.\n\n" +
                "To make the base really crispy and sturdy, flip the pizza over (onto baking paper/parchment) once the top has browned.\n\n" +
                "Once cooked, remove from the oven and add all the toppings you like. Make sure any meat is already cooked as this time it goes back into the oven just to heat up the toppings and melt the cheese. Bake again at 220C/425F for 5 minutes.");
        HashMap<String, String> ingredients12 = new HashMap<>();
        ingredients12.put("Shredded mozzarella", "170 g");
        ingredients12.put("almond flour", "85 g");
        ingredients12.put("cream cheese", "2 tablespoons");
        //ingredients12.put("egg", "1");
        ingredients12.put("salt", "A pinch");
        ingredients12.put("rosemary", "1/2 teaspoon");
        ingredients12.put("Pizza toppings", "your choice");
        mRef.child(key12).child("Ingredients").setValue(ingredients12);
        mRef.child(key12).child("Recipe ID").setValue(key12);

        AddStarterGroceryList();

    }

    private void AddStarterGroceryList(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mRef = mFirebaseDatabase.getReference("/grocery/" + userID);

        String key1 = mRef.push().getKey();
        mRef.child(key1).child("Ingredient Name").setValue(capitalizeFully("Milk"));
        mRef.child(key1).child("Ingredient Amount").setValue("1 quart");

        String key2 = mRef.push().getKey();
        mRef.child(key2).child("Ingredient Name").setValue(capitalizeFully("Eggs"));
        mRef.child(key2).child("Ingredient Amount").setValue("12");

        String key3 = mRef.push().getKey();
        mRef.child(key3).child("Ingredient Name").setValue(capitalizeFully("Chicken"));
        mRef.child(key3).child("Ingredient Amount").setValue("1 lb");

        startActivity(new Intent(SignupActivity.this, MainActivity.class));
    }

}

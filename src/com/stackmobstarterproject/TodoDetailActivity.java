/**
 * Copyright 2012-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmobstarterproject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.constants.Constants;
import com.model.Todo;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.exception.StackMobException;
import com.util.ActivityUtil;

import java.util.Map;
import java.util.Set;

/**
 * This Activity is to show the details of our Todo
 */
public class TodoDetailActivity extends BaseActivity {

  private EditText mEditTextTitle;
  private Button mDeleteBtn;
  private Button mSaveBtn;

  private Todo todo; // this is going to be the todo we want to show the details of

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_todo_detail);

    mEditTextTitle = (EditText) this.findViewById(R.id.atdEditTextTitle);
    mDeleteBtn = (Button) this.findViewById(R.id.atdDeleteBtn);
    mSaveBtn = (Button) this.findViewById(R.id.atdSaveBtn);

    // Get the data that were passed in from our main activity
    // Why don't we just pass in the ID and fetch that from StackMob?
    // We want to avoid making a lot of StackMob calls because Users might not like our app if it
    //   uses a lot of data :)
    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      // create a new todo from the data that are passed in from TodoActivity
      todo = new Todo(bundle.getString(Constants.TODO_TITLE));
      todo.setID(bundle.getString(Constants.TODO_ID)); // set the ID for our todo object
      mEditTextTitle.setText(todo.getTitle());
    } else {
      // there's no Bundle passed in, hence no todo to show
      // don't even show this page, finish this activity
      finish();
    }

    // add the `up` navigation for the home button on action bar
    ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
    }

    setOnClickListeners();
  }

  /**
   * Read about Android lifecycle for more information
   *
   * We're using SharedPreferences here to store the crucial data that we need to regenerate
   *   our view when user leaves the app and reopen it
   */
  @Override
  protected void onStart() {
    super.onStart();

    // we need to do this just in case user goes out of the app and reopen it
    if (todo == null) {
      final SharedPreferences prefs = getSharedPreferences(Constants.Pref.BASE_PREF,
          Context.MODE_PRIVATE);
      final String todoId = prefs.getString(Constants.Pref.TODO_ID, null);
      final String todoTitle = prefs.getString(Constants.Pref.TODO_TITLE, null);
      if (todoTitle != null && todoId != null) {
        todo = new Todo(todoTitle);
        todo.setID(todoId);
        mEditTextTitle.setText(todoTitle);
      } else {
        // there's no point of showing this page if there's no data to show
        finish();
      }
    }

    // move caret to end
    mEditTextTitle.setSelection(mEditTextTitle.getText().length());

    // show keyboard
    mEditTextTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (b) {
          getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
      }
    });

  }

  /**
   * This is when the app gets wiped out of the memory
   * We want to store some data so that we could regenerate view to the same state to where it was
   */
  @Override
  protected void onStop() {
    super.onStop();

    // store the title and id when user goes out of our app
    final SharedPreferences prefs = getSharedPreferences(Constants.Pref.BASE_PREF,
        Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = prefs.edit();

    // put the `todo` id and whatever the current content of our EditText
    editor.putString(Constants.Pref.TODO_ID, todo.getID())
          .putString(Constants.Pref.TODO_TITLE, mEditTextTitle.getText().toString())
          .commit();
  }

  private void setOnClickListeners() {
    mDeleteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // delete our todo from StackMob
        todo.destroy(new StackMobCallback() {
          @Override
          public void success(String s) {
            ActivityUtil.showToast(TodoDetailActivity.this, "Todo deleted");
          }

          @Override
          public void failure(StackMobException e) {
            ActivityUtil.showToast(TodoDetailActivity.this, "Error deleting todo: " + e.getMessage());
          }
        });

        // finish the activity
        ActivityUtil.finishActivity(TodoDetailActivity.this);
      }
    });

    mSaveBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // change the title (update it)
        todo.setTitle(mEditTextTitle.getText().toString());

        // send the todo with updated title to StackMob
        todo.save(new StackMobCallback() {
          @Override
          public void success(String s) {
            ActivityUtil.showToast(TodoDetailActivity.this, "Todo updated");
          }

          @Override
          public void failure(StackMobException e) {
            ActivityUtil.showToast(TodoDetailActivity.this, "Error updating todo: " + e.getMessage());
          }
        });

        // finish the activity
        ActivityUtil.finishActivity(TodoDetailActivity.this);
      }
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // No need for Menu on this page
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpTo(this, new Intent(this, TodoActivity.class));

        // add animation
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

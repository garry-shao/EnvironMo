package org.qmsos.environmo;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class CitySelectDialog extends DialogFragment implements OnEditorActionListener {

	public interface CitySelectListener {
        void onFinishCitySelectDialog(String cityName);
    }

	private EditText cityNameEditText;
	
	public CitySelectDialog() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		InputFilter noWhitespaceFilter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (Character.isSpaceChar(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		
		View view = inflater.inflate(R.layout.dialog_city_select, container);	
		cityNameEditText = (EditText) view.findViewById(R.id.city_name);
		cityNameEditText.setOnEditorActionListener(this);
		cityNameEditText.setFilters(new InputFilter[] { noWhitespaceFilter });
		cityNameEditText.requestFocus();
		
		getDialog().setTitle(R.string.dialog_city_select_title);
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		String cityName = cityNameEditText.getText().toString();

		CitySelectListener activity = (CitySelectListener) getActivity();
		activity.onFinishCitySelectDialog(cityName);
        
        dismiss();
        
        return true;
	}

}

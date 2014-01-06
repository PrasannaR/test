package com.cognizant.trumobi;


public interface PersonaDrawer {

	public int getVisibility();

	public boolean isOpaque();

	public boolean hasFocus();

	public boolean requestFocus();

	public void setTextFilterEnabled(boolean textFilterEnabled);
	public void clearTextFilter();


	public void setDragger(PersonaDragController dragger);
	public void setLauncher(PersonaLauncher personaLauncher);
	public void updateAppGrp();
	public void setNumColumns(int numColumns);
	public void setNumRows(int numRows);
	public void setPageHorizontalMargin(int margin);
	public void setAdapter(PersonaApplicationsAdapter adapter);
	public void setAnimationSpeed(int speed);
	public void open(boolean animate);
	public void close(boolean animate);
	public void setPadding(int left, int top, int right, int bottom);
}

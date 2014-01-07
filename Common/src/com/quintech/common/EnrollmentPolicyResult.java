package com.quintech.common;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentPolicyResult 
{
	public boolean succeeded = false;
	public List<String> failedPolicyItemIds = new ArrayList<String>();
	public String message = "";
}

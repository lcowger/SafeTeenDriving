package com.gmail.lcapps.safeteendriving;

import android.widget.Button;

public class TeenDriver
{
    private String m_name;
    private String m_token;
    private Button m_button;

    public void setName(String name)
    {
        m_name = name;
    }

    public void setToken(String token)
    {
        m_token = token;
    }

    public void setButton(Button button)
    {
        m_button = button;
    }

    public String getName()
    {
        return m_name;
    }

    public String getToken()
    {
        return m_token;
    }

    public Button getButton()
    {
        return m_button;
    }
}

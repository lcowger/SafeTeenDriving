package com.gmail.lcapps.safeteendriving;

public class TeenDriver
{
    private String m_name;
    private String m_id;
    private String m_guessId;
    private String m_token;
    private ServiceStatus m_serviceStatus;
    private TeenDriverSettings m_settings;

    public void setName(String name)
    {
        m_name = name;
    }

    public void setId(String id)
    {
        m_id = id;
    }

    public void setGuessId(String id)
    {
        m_guessId = id;
    }

    public void setToken(String token)
    {
        m_token = token;
    }

    public void setServiceStatus(ServiceStatus status)
    {
        m_serviceStatus = status;
    }

    public String getName()
    {
        return m_name;
    }

    public String getId() { return m_id; }

    public String getGuessId() { return m_guessId; }

    public String getToken()
    {
        return m_token;
    }

    public ServiceStatus getServerStatus()
    {
        return m_serviceStatus;
    }
}

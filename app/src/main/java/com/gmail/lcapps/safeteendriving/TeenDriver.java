package com.gmail.lcapps.safeteendriving;

public class TeenDriver
{
    private String m_name;
    private String m_id;
    private String m_guessId;
    private ServiceStatus m_serviceStatus;
    private TeenDriverSettings m_settings;

    public TeenDriver()
    {
        m_name = "";
        m_id = "";
        m_guessId = "";
        m_serviceStatus = null;
        m_settings = new TeenDriverSettings();
    }

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

    public ServiceStatus getServerStatus()
    {
        return m_serviceStatus;
    }
}

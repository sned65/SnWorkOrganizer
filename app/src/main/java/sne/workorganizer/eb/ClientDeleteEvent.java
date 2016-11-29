package sne.workorganizer.eb;

import sne.workorganizer.db.Client;

/**
 * Created by Nikolay_Smirnov on 23.11.2016.
 */
public class ClientDeleteEvent
{
    private final Client _client;

    public ClientDeleteEvent(Client client)
    {
        _client = client;
    }

    public Client getClient()
    {
        return _client;
    }
}

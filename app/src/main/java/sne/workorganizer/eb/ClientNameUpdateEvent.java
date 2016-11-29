package sne.workorganizer.eb;

import sne.workorganizer.db.Client;

/**
 * Created by Nikolay_Smirnov on 23.11.2016.
 */

public class ClientNameUpdateEvent
{
    private final Client _client;

    public ClientNameUpdateEvent(Client client)
    {
        _client = client;
    }

    public Client getClient()
    {
        return _client;
    }

}

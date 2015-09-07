package com.helger.pyp.indexer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.microdom.IMicroDocument;
import com.helger.commons.microdom.IMicroElement;
import com.helger.commons.microdom.MicroDocument;
import com.helger.commons.microdom.convert.MicroTypeConverter;
import com.helger.commons.microdom.serialize.MicroReader;
import com.helger.commons.microdom.serialize.MicroWriter;
import com.helger.commons.scope.IScope;
import com.helger.commons.scope.singleton.AbstractGlobalSingleton;
import com.helger.commons.state.EChange;
import com.helger.commons.state.ESuccess;
import com.helger.commons.string.ToStringGenerator;
import com.helger.datetime.PDTFactory;
import com.helger.peppol.identifier.IParticipantIdentifier;
import com.helger.peppol.identifier.participant.IPeppolParticipantIdentifier;
import com.helger.photon.basic.app.io.WebFileIO;

/**
 * The global indexer manager that takes an item for queuing and maintains the
 * uniqueness of the items to queue.
 *
 * @author Philip Helger
 */
public final class IndexerManager extends AbstractGlobalSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (IndexerManager.class);
  private static final String ELEMENT_ROOT = "root";
  private static final String ELEMENT_ITEM = "item";

  @GuardedBy ("m_aRWLock")
  private final Set <IndexerWorkItem> m_aUniqueItems = new HashSet <> ();
  @GuardedBy ("m_aRWLock")
  private final ReIndexWorkQueue m_aReIndexList = new ReIndexWorkQueue ();
  private final IndexerWorkQueue m_aIndexerWorkQueue = new IndexerWorkQueue (this::_fetchParticipantDataSynchronously);

  @Nonnull
  private static File _getIndexerWorkItemFile ()
  {
    return WebFileIO.getDataIO ().getFile ("indexer-work-queue.xml");
  }

  @Deprecated
  @UsedViaReflection
  public IndexerManager ()
  {
    // Read an eventually existing serialized element
    final IMicroDocument aDoc = MicroReader.readMicroXML (_getIndexerWorkItemFile ());
    if (aDoc != null)
    {
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Reading persisted indexer work items from " + _getIndexerWorkItemFile ());
      for (final IMicroElement eItem : aDoc.getDocumentElement ().getAllChildElements (ELEMENT_ITEM))
      {
        final IndexerWorkItem aItem = MicroTypeConverter.convertToNative (eItem, IndexerWorkItem.class);
        m_aUniqueItems.add (aItem);
        m_aIndexerWorkQueue.queueObject (aItem);
      }
    }
  }

  /**
   * @return The global instance of this class. Never <code>null</code>.
   */
  @Nonnull
  public static IndexerManager getInstance ()
  {
    return getGlobalSingleton (IndexerManager.class);
  }

  private static void _write (@Nonnull final List <IndexerWorkItem> aItems)
  {
    if (!aItems.isEmpty ())
    {
      s_aLogger.info ("Persisting " + aItems.size () + " indexer work items");
      final IMicroDocument aDoc = new MicroDocument ();
      final IMicroElement eRoot = aDoc.appendElement (ELEMENT_ROOT);
      for (final IndexerWorkItem aItem : aItems)
        eRoot.appendChild (MicroTypeConverter.convertToMicroElement (aItem, ELEMENT_ITEM));
      if (MicroWriter.writeToFile (aDoc, _getIndexerWorkItemFile ()).isFailure ())
        throw new IllegalStateException ("Failed to write IndexerWorkItems to " + _getIndexerWorkItemFile ());
    }
  }

  @Override
  protected void onBeforeDestroy (@Nonnull final IScope aScopeToBeDestroyed)
  {
    // Get all remaining objects and save them for late reuse
    final List <IndexerWorkItem> aRemainingItems = m_aIndexerWorkQueue.stop ();
    _write (aRemainingItems);
  }

  @Nonnull
  public EChange queueObject (@Nonnull final IParticipantIdentifier aParticipantID,
                              @Nonnull final EIndexerWorkItemType eType)
  {
    // Build item
    final IndexerWorkItem aItem = new IndexerWorkItem (aParticipantID, eType);

    // Check for duplicate
    m_aRWLock.writeLock ().lock ();
    try
    {
      if (!m_aUniqueItems.add (aItem))
      {
        s_aLogger.info ("Ignoring item " + aItem + " because it is already in the queue!");
        return EChange.UNCHANGED;
      }
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    // Queue it
    m_aIndexerWorkQueue.queueObject (aItem);
    return EChange.CHANGED;
  }

  private void _removeFromOverallList (@Nonnull final IndexerWorkItem aItem)
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      m_aUniqueItems.remove (aItem);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  @Nonnull
  private ESuccess _onCreateOrUpdate (@Nonnull final IPeppolParticipantIdentifier aParticipantID)
  {
    s_aLogger.info ("On participant create/update: " + aParticipantID.getURIEncoded ());
    // TODO
    return ESuccess.FAILURE;
  }

  @Nonnull
  private ESuccess _onDelete (@Nonnull final IPeppolParticipantIdentifier aParticipantID)
  {
    s_aLogger.info ("On participant delete: " + aParticipantID.getURIEncoded ());
    // TODO
    return ESuccess.FAILURE;
  }

  @Nonnull
  private ESuccess _fetchParticipantData0 (@Nonnull final IndexerWorkItem aItem)
  {
    ESuccess eSuccess;
    switch (aItem.getType ())
    {
      case CREATE_UPDATE:
        eSuccess = _onCreateOrUpdate (aItem.getParticipantID ());
        break;
      case DELETE:
        eSuccess = _onDelete (aItem.getParticipantID ());
        break;
      default:
        throw new IllegalStateException ("Unsupported item type: " + aItem);
    }

    if (eSuccess.isSuccess ())
    {
      // Item handled - remove from overall list
      _removeFromOverallList (aItem);
      return ESuccess.SUCCESS;
    }

    return ESuccess.FAILURE;
  }

  private void _addToReIndexList (@Nonnull final ReIndexWorkItem aReIndexItem)
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      m_aReIndexList.addItem (aReIndexItem);
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
    s_aLogger.info ("Added " +
                    aReIndexItem.getWorkItem ().getType () +
                    " of " +
                    aReIndexItem.getWorkItem ().getParticipantID ().getURIEncoded () +
                    " to re-try list");
  }

  /**
   * This is the main method to perform the operation on the SMP.
   *
   * @param aItem
   *        The item to be fetched. Never <code>null</code>.
   * @return {@link ESuccess}.
   */
  @Nonnull
  private ESuccess _fetchParticipantDataSynchronously (@Nonnull final IndexerWorkItem aItem)
  {
    final ESuccess eSuccess = _fetchParticipantData0 (aItem);

    if (eSuccess.isFailure ())
    {
      // Failed to fetch participant data - add to re-index queue and leave in
      // the overall list
      _addToReIndexList (new ReIndexWorkItem (aItem));
    }
    return eSuccess;
  }

  public void expireOldEntries ()
  {
    m_aRWLock.writeLock ().lock ();
    try
    {
      // Expire old entries
      final List <ReIndexWorkItem> aExpiredItems = m_aReIndexList.getAndRemoveAllExpiredEntries ();
      if (!aExpiredItems.isEmpty ())
      {
        s_aLogger.info ("Expired " + aExpiredItems.size () + " re-index work items");

        // remove them from the overall list
        aExpiredItems.stream ().forEach (aItem -> m_aUniqueItems.remove (aItem.getWorkItem ()));
      }
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }
  }

  public void reIndexParticipantData ()
  {
    List <ReIndexWorkItem> aReIndexNowItems;
    m_aRWLock.writeLock ().lock ();
    try
    {
      // Get and remove all items to re-index "now"
      aReIndexNowItems = m_aReIndexList.getAndRemoveAllItemsForReIndex (PDTFactory.getCurrentLocalDateTime ());
    }
    finally
    {
      m_aRWLock.writeLock ().unlock ();
    }

    for (final ReIndexWorkItem aReIndexItem : aReIndexNowItems)
      if (_fetchParticipantData0 (aReIndexItem.getWorkItem ()).isFailure ())
      {
        m_aRWLock.writeLock ().lock ();
        try
        {
          // Still no success
          aReIndexItem.incRetryCount ();
        }
        finally
        {
          m_aRWLock.writeLock ().unlock ();
        }

        // Add again to the retry list
        _addToReIndexList (aReIndexItem);
      }
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (super.toString ()).append ("UniqueItems", m_aUniqueItems)
                                                    .append ("ReIndexList", m_aReIndexList)
                                                    .append ("IndexerWorkQueue", m_aIndexerWorkQueue)
                                                    .toString ();
  }
}

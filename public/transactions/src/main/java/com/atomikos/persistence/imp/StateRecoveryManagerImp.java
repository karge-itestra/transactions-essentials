/**
 * Copyright (C) 2000-2023 Atomikos <info@atomikos.com>
 *
 * LICENSE CONDITIONS
 *
 * See http://www.atomikos.com/Main/WhichLicenseApplies for details.
 */

package com.atomikos.persistence.imp;

import com.atomikos.finitestates.FSMEnterEvent;
import com.atomikos.finitestates.FSMEnterListener;
import com.atomikos.persistence.RecoverableCoordinator;
import com.atomikos.persistence.StateRecoveryManager;
import com.atomikos.recovery.LogException;
import com.atomikos.recovery.OltpLog;
import com.atomikos.recovery.PendingTransactionRecord;
import com.atomikos.recovery.TxState;
import com.atomikos.util.Assert;

/**
 * Default implementation of a state recovery manager.
 */

public class StateRecoveryManagerImp  implements StateRecoveryManager, FSMEnterListener
{
	/**
	 * @see StateRecoveryManager
	 */
	public void register(RecoverableCoordinator staterecoverable) {
		Assert.notNull("illegal attempt to register null staterecoverable", staterecoverable);
		TxState[] states = TxState.values();
		for (TxState txState : states) {
			if (txState.isRecoverableState()) {
				staterecoverable.addFSMEnterListener(this, txState);
			}
		}	
	}

	/**
	 * @see FSMPreEnterListener
	 */
	public void preEnter(FSMEnterEvent event) throws IllegalStateException {
		TxState state = event.getState();
		
		RecoverableCoordinator source = (RecoverableCoordinator) event.getSource();
		PendingTransactionRecord pendingTransactionRecord = source.getPendingTransactionRecord(state);
		if (pendingTransactionRecord != null) {
			// null images are not logged as per the Recoverable contract
			try {
				oltpLog.write(pendingTransactionRecord);

			} catch (Exception le) {
				throw new IllegalStateException("could not flush state image " + le.getMessage() + " " + le.getClass().getName(), le);
			} 
		}

	}

	/**
	 * @see StateRecoveryManager
	 */
	public void close() throws LogException {
		oltpLog.close();
	}
	
	private OltpLog oltpLog;
	
	public void setOltpLog(OltpLog oltpLog) {
		this.oltpLog = oltpLog;
	}

	@Override
	public void entered(FSMEnterEvent e) {}
	

	

}

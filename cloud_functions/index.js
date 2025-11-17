const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

// Helper function to calculate string similarity
function calculateSimilarity(str1, str2) {
  const longer = str1.length > str2.length ? str1 : str2;
  const shorter = str1.length > str2.length ? str2 : str1;
  
  if (longer.length === 0) return 1.0;
  
  const editDistance = getEditDistance(longer, shorter);
  return (longer.length - editDistance) / longer.length;
}

function getEditDistance(str1, str2) {
  str1 = str1.toLowerCase();
  str2 = str2.toLowerCase();
  
  const costs = [];
  for (let i = 0; i <= str1.length; i++) {
    let lastValue = i;
    for (let j = 0; j <= str2.length; j++) {
      if (i === 0) {
        costs[j] = j;
      } else if (j > 0) {
        let newValue = costs[j - 1];
        if (str1.charAt(i - 1) !== str2.charAt(j - 1)) {
          newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
        }
        costs[j - 1] = lastValue;
        lastValue = newValue;
      }
    }
    if (i > 0) costs[str2.length] = lastValue;
  }
  return costs[str2.length];
}

// Helper function to check if items match
function itemsMatch(item1, item2) {
  // Must have same category
  if (item1.category !== item2.category) {
    return false;
  }
  
  // Check location match
  const locationMatch = item1.location.toLowerCase() === item2.location.toLowerCase();
  
  // Check title similarity
  const titleSimilarity = calculateSimilarity(item1.title, item2.title);
  const titleMatch = titleSimilarity > 0.6; // 60% similarity threshold
  
  // Match if same location OR similar titles
  return locationMatch || titleMatch;
}

// Helper function to send notification
async function sendMatchNotification(userId, title, body, data) {
  try {
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) return;
    
    const userData = userDoc.data();
    const fcmToken = userData.fcmToken;
    
    if (!fcmToken) return;
    
    const message = {
      notification: {
        title: title,
        body: body
      },
      data: data,
      token: fcmToken
    };
    
    await messaging.send(message);
    console.log('Notification sent successfully to', userId);
  } catch (error) {
    console.error('Error sending notification:', error);
  }
}

// Cloud Function: Match found items with lost items
exports.matchFoundItem = functions.firestore
  .document('found_items/{itemId}')
  .onCreate(async (snap, context) => {
    const foundItem = snap.data();
    const foundItemId = context.params.itemId;
    
    console.log('New found item:', foundItemId, foundItem.title);
    
    try {
      // Query lost items with same category
      const lostItemsSnapshot = await db.collection('lost_items')
        .where('category', '==', foundItem.category)
        .where('isActive', '==', true)
        .get();
      
      if (lostItemsSnapshot.empty) {
        console.log('No matching lost items found');
        return null;
      }
      
      // Check each lost item for match
      const matchPromises = [];
      lostItemsSnapshot.forEach((lostDoc) => {
        const lostItem = lostDoc.data();
        const lostItemId = lostDoc.id;
        
        if (itemsMatch(foundItem, lostItem)) {
          console.log('Match found:', lostItemId, 'with', foundItemId);
          
          // Create match document
          const matchRef = db.collection('matches').doc();
          const matchData = {
            id: matchRef.id,
            lostItemId: lostItemId,
            foundItemId: foundItemId,
            lostUserId: lostItem.userId,
            foundUserId: foundItem.userId,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            notificationSent: true
          };
          
          matchPromises.push(matchRef.set(matchData));
          
          // Send notification to lost item owner
          matchPromises.push(
            sendMatchNotification(
              lostItem.userId,
              'Potential Match Found!',
              `A found item may match your lost item: ${lostItem.title}`,
              {
                foundItemId: foundItemId,
                lostItemId: lostItemId,
                matchId: matchRef.id
              }
            )
          );
        }
      });
      
      await Promise.all(matchPromises);
      return null;
    } catch (error) {
      console.error('Error in matchFoundItem:', error);
      return null;
    }
  });

// Cloud Function: Match lost items with found items
exports.matchLostItem = functions.firestore
  .document('lost_items/{itemId}')
  .onCreate(async (snap, context) => {
    const lostItem = snap.data();
    const lostItemId = context.params.itemId;
    
    console.log('New lost item:', lostItemId, lostItem.title);
    
    try {
      // Query found items with same category
      const foundItemsSnapshot = await db.collection('found_items')
        .where('category', '==', lostItem.category)
        .where('isActive', '==', true)
        .get();
      
      if (foundItemsSnapshot.empty) {
        console.log('No matching found items');
        return null;
      }
      
      // Check each found item for match
      const matchPromises = [];
      foundItemsSnapshot.forEach((foundDoc) => {
        const foundItem = foundDoc.data();
        const foundItemId = foundDoc.id;
        
        if (itemsMatch(lostItem, foundItem)) {
          console.log('Match found:', lostItemId, 'with', foundItemId);
          
          // Create match document
          const matchRef = db.collection('matches').doc();
          const matchData = {
            id: matchRef.id,
            lostItemId: lostItemId,
            foundItemId: foundItemId,
            lostUserId: lostItem.userId,
            foundUserId: foundItem.userId,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            notificationSent: true
          };
          
          matchPromises.push(matchRef.set(matchData));
          
          // Send notification to lost item owner
          matchPromises.push(
            sendMatchNotification(
              lostItem.userId,
              'Potential Match Found!',
              `A found item may match your lost item: ${lostItem.title}`,
              {
                foundItemId: foundItemId,
                lostItemId: lostItemId,
                matchId: matchRef.id
              }
            )
          );
        }
      });
      
      await Promise.all(matchPromises);
      return null;
    } catch (error) {
      console.error('Error in matchLostItem:', error);
      return null;
    }
  });

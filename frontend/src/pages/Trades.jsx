// frontend/src/pages/Trades.jsx
import { useCallback, useState } from 'react';

function Trades({ trades }) {
  const [selectedId, setSelectedId] = useState(null);

  // Reference-stable across renders — onClick prop on <TradeRow> won't change
  const handleSelect = useCallback((id) => setSelectedId(id), []);

  return (
    <DataTable data={trades}>
      <DataTable.Header columns={cols} />
      <DataTable.Body
        renderRow={(t) => <TradeRow key={t.id} trade={t} onClick={handleSelect} />}
      />
    </DataTable>
  );
}

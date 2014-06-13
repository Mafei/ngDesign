-------------------------------------------------------------------------------
-- Copyright (c) 2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Matthieu Wipliez - initial API and implementation and/or initial documentation
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Flip-flop synchronizer
-- Author     : Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;

-------------------------------------------------------------------------------

entity SynchronizerFF is
  generic (
    stages : integer := 2);
  port (
    reset_n    : in  std_logic;
    din_clock  : in  std_logic;
    dout_clock : in  std_logic;
    din        : in  std_logic;
    dout       : out std_logic
  );
end SynchronizerFF;

-------------------------------------------------------------------------------

architecture arch_Synchronizer_ff of SynchronizerFF is

  -----------------------------------------------------------------------------
  -- Internal signal declarations
  -----------------------------------------------------------------------------
  signal ff : std_logic_vector(stages - 1 downto 0);

begin

  dout <= ff(stages - 1);

  process(reset_n, dout_clock)
  begin
    if reset_n = '0' then
      ff   <= (others => '0');
    elsif rising_edge(dout_clock) then
      -- N-stage shift register
      for i in stages - 1 downto 1 loop
        ff(i) <= ff(i - 1);
      end loop;
      ff(0) <= din;
    end if;
  end process;

end arch_Synchronizer_ff;
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
-- Title      : Mux synchronizer
-- Author     : Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;

-------------------------------------------------------------------------------

entity SynchronizerMux is
  generic (
    width  : integer := 32;
    stages : integer := 2);
  port (
    reset_n    : in  std_logic;
    din_clock  : in  std_logic;
    dout_clock : in  std_logic;
    din_send   : in  std_logic;
    din        : in  std_logic_vector(width - 1 downto 0);
    dout       : out std_logic_vector(width - 1 downto 0)
  );
end SynchronizerMux;

-------------------------------------------------------------------------------

architecture arch_Synchronizer_mux of SynchronizerMux is

  -----------------------------------------------------------------------------
  -- Internal signal declarations
  -----------------------------------------------------------------------------
  signal control_sync : std_logic;

begin

  sync: entity work.SynchronizerFF
    generic map (
      stages => stages)
    port map (
      reset_n    => reset_n,
      din_clock  => din_clock,
      dout_clock => dout_clock,
      din        => din_send,
      dout       => control_sync
    );

  process(reset_n, dout_clock)
  begin
    if reset_n = '0' then
      dout <= (others => '0');
    elsif rising_edge(dout_clock) then
      if (control_sync = '1') then
        dout <= din;
      end if;
    end if;
  end process;

end arch_Synchronizer_mux;
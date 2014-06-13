-------------------------------------------------------------------------------
-- Copyright (c) 2012-2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Nicolas Siret - initial API and implementation and/or initial documentation
--    Matthieu Wipliez - refactoring and maintenance
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Pseudo dual-port RAM
-- Author     : Nicolas Siret (nicolas.siret@synflow.com)
--              Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
-------------------------------------------------------------------------------



-------------------------------------------------------------------------------
-- Entity
-------------------------------------------------------------------------------
entity DualPortRAM is
  generic (
    depth : integer := 8;
    width : integer := 16;
    initVal: integer := 0);
  port (
    wr_clock        : in  std_logic;
    rd_clock        : in  std_logic;
    reset_n         : in  std_logic;
    --
    wr_address      : in  std_logic_vector(depth - 1 downto 0);
    data            : in  std_logic_vector(width - 1 downto 0);
    data_send       : in  std_logic;
    rd_address      : in  std_logic_vector(depth - 1 downto 0);
    q               : out std_logic_vector(width - 1 downto 0));
end DualPortRAM;
-------------------------------------------------------------------------------



-------------------------------------------------------------------------------
-- Architecture
-------------------------------------------------------------------------------
architecture arch_Dual_Port_RAM of DualPortRAM is

  -----------------------------------------------------------------------------
  -- Internal type declarations
  -----------------------------------------------------------------------------
  type ram_type is array (0 to 2**depth - 1) of std_logic_vector(width - 1 downto 0);

  -----------------------------------------------------------------------------
  -- Internal signal declarations
  -----------------------------------------------------------------------------
  shared variable ram : ram_type := (others => std_logic_vector(to_signed(initVal, width)));

-------------------------------------------------------------------------------
begin

  readData : process (rd_clock)
  begin
    if rising_edge(rd_clock) then
      q <= ram(to_integer(unsigned(rd_address)));
    end if;
  end process readData;

  writeData : process (wr_clock)
  begin
    if rising_edge(wr_clock) then
      if data_send = '1' then
        ram(to_integer(unsigned(wr_address))) := data;
      end if;
    end if;
  end process writeData;

end arch_Dual_Port_RAM;

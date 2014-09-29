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
-- Title      : Single-port inferred RAM
-- Author     : Nicolas Siret (nicolas.siret@synflow.com)
--              Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-------------------------------------------------------------------------------
-- Entity
-------------------------------------------------------------------------------
entity SinglePortRAM is
  generic (
    depth   : integer := 8;
    width   : integer := 16;
    numAdditionalRegisters : integer := 0);
  port (
    clock           : in  std_logic;
    --
    address         : in  std_logic_vector(depth - 1 downto 0);
    data            : in  std_logic_vector(width - 1 downto 0);
    data_send    : in  std_logic;
    q               : out std_logic_vector(width - 1 downto 0));
end SinglePortRAM;
-------------------------------------------------------------------------------


-------------------------------------------------------------------------------
-- Architecture
-------------------------------------------------------------------------------
architecture arch_Single_Port_RAM of SinglePortRAM is

  -----------------------------------------------------------------------------
  -- Internal type declarations
  -----------------------------------------------------------------------------
  type ram_type is array (0 to 2**depth - 1) of std_logic_vector(width - 1 downto 0);

  -----------------------------------------------------------------------------
  -- Internal signal declarations
  -----------------------------------------------------------------------------
  shared variable ram : ram_type;

-------------------------------------------------------------------------------  
begin

  -- read and write data process
  rdwrData : process (clock)
  begin
    if rising_edge(clock) then
      if data_send = '1' then
        ram(to_integer(unsigned(address))) := data;
        q <= data;
      else
        q <= ram(to_integer(unsigned(address)));
      end if;
    end if;
  end process rdwrData;

end arch_Single_Port_RAM;